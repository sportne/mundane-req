package mundanereq.trace;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import mundanereq.Interpreter;

/** Disposable decomposition indexes and deterministic trace operations. */
public final class TraceAnalyzer {
    public record PathResult(String id, List<String> path) {
        public PathResult {
            path = List.copyOf(path);
        }

        public int distance() {
            return path.size() - 1;
        }
    }

    public record TransitiveResult(List<PathResult> paths, List<List<String>> cycles) {
        public TransitiveResult {
            paths = List.copyOf(paths);
            cycles = cycles.stream().map(List::copyOf).toList();
        }
    }

    private static final Comparator<List<String>> PATH_ORDER = (left, right) -> {
        for (int index = 0; index < left.size(); index++) {
            int element = left.get(index).compareTo(right.get(index));
            if (element != 0) return element;
        }
        return Integer.compare(left.size(), right.size());
    };

    private final Set<String> ids;
    private final Map<String, List<String>> outgoing;
    private final Map<String, List<String>> incoming;

    public TraceAnalyzer(Interpreter.Result result) {
        ids = Set.copyOf(result.byId().keySet());
        Map<String, Set<String>> outgoingSets = new HashMap<>();
        ids.forEach(id -> outgoingSets.put(id, new TreeSet<>(result.outgoing().getOrDefault(id, Set.of()))));
        outgoing = immutableSorted(outgoingSets);

        Map<String, Set<String>> incomingSets = new HashMap<>();
        ids.forEach(id -> incomingSets.put(id, new TreeSet<>()));
        outgoing.forEach((child, parents) -> parents.forEach(parent -> incomingSets.get(parent).add(child)));
        incoming = immutableSorted(incomingSets);
    }

    private static Map<String, List<String>> immutableSorted(Map<String, Set<String>> source) {
        Map<String, List<String>> result = new TreeMap<>();
        source.forEach((id, targets) -> result.put(id, List.copyOf(targets)));
        return Map.copyOf(result);
    }

    public List<String> parents(String id) {
        return outgoing.getOrDefault(id, List.of());
    }

    public List<String> children(String id) {
        return incoming.getOrDefault(id, List.of());
    }

    public TransitiveResult higher(String id) {
        Map<String, Integer> distances = distances(id, outgoing);
        Map<String, List<String>> bestPaths = higherPaths(id, distances);
        List<PathResult> paths = new ArrayList<>();
        bestPaths.forEach((target, path) -> {
            if (!target.equals(id)) paths.add(new PathResult(target, path));
        });
        paths.sort(Comparator.comparingInt(PathResult::distance).thenComparing(PathResult::id));
        return new TransitiveResult(paths, cyclicComponents(distances.keySet()));
    }

    public TransitiveResult impact(String id) {
        Map<String, Integer> distances = distances(id, incoming);
        Map<String, List<String>> bestPaths = impactPaths(id, distances);
        List<PathResult> paths = new ArrayList<>();
        bestPaths.forEach((child, path) -> {
            if (!child.equals(id)) paths.add(new PathResult(child, path));
        });
        paths.sort(Comparator.comparingInt(PathResult::distance).thenComparing(PathResult::id));
        return new TransitiveResult(paths, cyclicComponents(distances.keySet()));
    }

    private static Map<String, Integer> distances(String start, Map<String, List<String>> adjacency) {
        Map<String, Integer> result = new HashMap<>();
        ArrayDeque<String> pending = new ArrayDeque<>();
        result.put(start, 0);
        pending.add(start);
        while (!pending.isEmpty()) {
            String current = pending.removeFirst();
            int nextDistance = result.get(current) + 1;
            for (String next : adjacency.getOrDefault(current, List.of())) {
                if (result.putIfAbsent(next, nextDistance) == null) pending.addLast(next);
            }
        }
        return Map.copyOf(result);
    }

    private Map<String, List<String>> higherPaths(String start, Map<String, Integer> distances) {
        Map<String, List<String>> paths = new HashMap<>();
        paths.put(start, List.of(start));
        for (String id : orderedByDistance(distances)) {
            if (id.equals(start)) continue;
            List<String> best = null;
            for (String predecessor : incoming.getOrDefault(id, List.of())) {
                if (distances.getOrDefault(predecessor, -1) != distances.get(id) - 1) continue;
                List<String> candidate = new ArrayList<>(paths.get(predecessor));
                candidate.add(id);
                if (best == null || PATH_ORDER.compare(candidate, best) < 0) best = List.copyOf(candidate);
            }
            if (best == null) throw new IllegalStateException("reachable requirement has no shortest predecessor");
            paths.put(id, best);
        }
        return Map.copyOf(paths);
    }

    private Map<String, List<String>> impactPaths(String target, Map<String, Integer> distances) {
        Map<String, List<String>> paths = new HashMap<>();
        paths.put(target, List.of(target));
        for (String id : orderedByDistance(distances)) {
            if (id.equals(target)) continue;
            List<String> best = null;
            for (String next : outgoing.getOrDefault(id, List.of())) {
                if (distances.getOrDefault(next, -1) != distances.get(id) - 1) continue;
                List<String> candidate = new ArrayList<>();
                candidate.add(id);
                candidate.addAll(paths.get(next));
                if (best == null || PATH_ORDER.compare(candidate, best) < 0) best = List.copyOf(candidate);
            }
            if (best == null) throw new IllegalStateException("reachable requirement has no shortest successor");
            paths.put(id, best);
        }
        return Map.copyOf(paths);
    }

    private static List<String> orderedByDistance(Map<String, Integer> distances) {
        return distances.keySet().stream()
                .sorted(Comparator.<String>comparingInt(distances::get).thenComparing(Comparator.naturalOrder()))
                .toList();
    }

    private List<List<String>> cyclicComponents(Set<String> scope) {
        List<String> finishOrder = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        for (String id : new TreeSet<>(scope)) finishIterative(id, scope, visited, finishOrder);

        visited.clear();
        List<List<String>> cycles = new ArrayList<>();
        for (int index = finishOrder.size() - 1; index >= 0; index--) {
            String id = finishOrder.get(index);
            if (visited.contains(id)) continue;
            List<String> component = new ArrayList<>();
            collectIterative(id, scope, visited, component);
            component.sort(String::compareTo);
            if (component.size() > 1 || outgoing.getOrDefault(id, List.of()).contains(id)) {
                cycles.add(List.copyOf(component));
            }
        }
        cycles.sort(Comparator.comparing(List::getFirst));
        return List.copyOf(cycles);
    }

    private void finishIterative(String start, Set<String> scope, Set<String> visited, List<String> order) {
        if (!visited.add(start)) return;
        ArrayDeque<FinishFrame> stack = new ArrayDeque<>();
        stack.push(new FinishFrame(start, 0));
        while (!stack.isEmpty()) {
            FinishFrame frame = stack.pop();
            List<String> neighbors = outgoing.getOrDefault(frame.id(), List.of());
            if (frame.index() < neighbors.size()) {
                stack.push(new FinishFrame(frame.id(), frame.index() + 1));
                String next = neighbors.get(frame.index());
                if (scope.contains(next) && visited.add(next)) stack.push(new FinishFrame(next, 0));
            } else {
                order.add(frame.id());
            }
        }
    }

    private void collectIterative(String start, Set<String> scope, Set<String> visited, List<String> component) {
        ArrayDeque<String> stack = new ArrayDeque<>();
        stack.push(start);
        visited.add(start);
        while (!stack.isEmpty()) {
            String id = stack.pop();
            component.add(id);
            List<String> neighbors = incoming.getOrDefault(id, List.of());
            for (int index = neighbors.size() - 1; index >= 0; index--) {
                String next = neighbors.get(index);
                if (scope.contains(next) && visited.add(next)) stack.push(next);
            }
        }
    }

    private record FinishFrame(String id, int index) {}
}
