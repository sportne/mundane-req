package mundanereq;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Bounded ReqIF 1.2 profile adapter for the provisional mundanereq 0.1 model. */
public final class ReqifProbe {
    static final String REQIF_NS = "http://www.omg.org/spec/ReqIF/20110401/reqif.xsd";
    static final String XHTML_NS = "http://www.w3.org/1999/xhtml";
    static final String PROFILE = "mundanereq-reqif-profile-0.1";
    static final String FIXED_ROUNDTRIP_TIME = "2000-01-01T00:00:00Z";

    private static final String DT_STRING = "MR_DT_STRING";
    private static final String DT_XHTML = "MR_DT_XHTML";
    private static final String TYPE_REQUIREMENT = "MR_TYPE_REQUIREMENT";
    private static final String TYPE_DECOMPOSES = "MR_TYPE_DECOMPOSES";
    private static final String TYPE_SPECIFICATION = "MR_TYPE_SPECIFICATION";
    private static final String AD_ID = "MR_AD_ID";
    private static final String AD_TITLE = "MR_AD_TITLE";
    private static final String AD_ALLOCATION = "MR_AD_ALLOCATION";
    private static final String AD_STATEMENT = "MR_AD_STATEMENT";
    private static final String AD_RATIONALE = "MR_AD_RATIONALE";
    private static final String AD_SOURCE = "MR_AD_SOURCE";

    private ReqifProbe() {}

    public static void main(String[] args) {
        System.exit(run(args, System.out, System.err));
    }

    static int run(String[] args, PrintStream out, PrintStream err) {
        if (args.length == 0 || args[0].equals("--help") || args[0].equals("-h")) {
            out.println(usage());
            return args.length == 0 ? 2 : 0;
        }
        try {
            return switch (args[0]) {
                case "export" -> runExport(args, out, err);
                case "import-inventory" -> runImport(args, out, err);
                case "roundtrip" -> runRoundtrip(args, out, err);
                default -> {
                    err.println("unknown operation: " + args[0]);
                    err.println(usage());
                    yield 2;
                }
            };
        } catch (IllegalArgumentException | IOException exception) {
            err.println("reqif: " + exception.getMessage());
            return 1;
        } catch (Exception exception) {
            err.println("reqif: " + exception.getClass().getSimpleName() + ": " + exception.getMessage());
            return 1;
        }
    }

    private static int runExport(String[] args, PrintStream out, PrintStream err) throws Exception {
        if (args.length < 4) {
            err.println(usage());
            return 2;
        }
        String timestamp = canonicalTimestamp(args[1]);
        Path output = Path.of(args[2]);
        Probe.Result result = interpretPaths(args, 3);
        if (!result.diagnostics().isEmpty()) return printDiagnostics(result, err);
        byte[] xml = exportReqif(result.requirements(), timestamp);
        Files.write(output, xml);
        out.printf(
                "Exported %d requirements and %d decomposition relationships to %s.%n",
                result.requirements().size(), relationshipCount(result.requirements()), output);
        return 0;
    }

    private static int runImport(String[] args, PrintStream out, PrintStream err) throws Exception {
        if (args.length != 2) {
            err.println(usage());
            return 2;
        }
        List<Probe.Requirement> requirements = importReqif(Files.readAllBytes(Path.of(args[1])), args[1]);
        out.print(Probe.normalizedInventory(requirements));
        return 0;
    }

    private static int runRoundtrip(String[] args, PrintStream out, PrintStream err) throws Exception {
        if (args.length < 2) {
            err.println(usage());
            return 2;
        }
        Probe.Result result = interpretPaths(args, 1);
        if (!result.diagnostics().isEmpty()) return printDiagnostics(result, err);
        byte[] xml = exportReqif(result.requirements(), FIXED_ROUNDTRIP_TIME);
        List<Probe.Requirement> imported = importReqif(xml, "<roundtrip>");
        String expected = Probe.normalizedInventory(result.requirements());
        String actual = Probe.normalizedInventory(imported);
        if (!expected.equals(actual)) {
            err.println("reqif: semantic inventory changed during roundtrip");
            return 1;
        }
        out.printf(
                "Roundtrip preserved %d requirements and %d decomposition relationships.%n",
                imported.size(), relationshipCount(imported));
        return 0;
    }

    private static Probe.Result interpretPaths(String[] args, int start) {
        List<Path> inputs = new ArrayList<>();
        for (int index = start; index < args.length; index++) inputs.add(Path.of(args[index]));
        return Probe.interpretInputs(inputs);
    }

    private static int printDiagnostics(Probe.Result result, PrintStream err) {
        result.diagnostics().forEach(diagnostic -> err.println(diagnostic.formatted()));
        return 1;
    }

    private static String usage() {
        return "Usage:\n"
                + "  reqifprobe export TIMESTAMP OUTPUT.reqif FILE_OR_DIRECTORY...\n"
                + "  reqifprobe import-inventory INPUT.reqif\n"
                + "  reqifprobe roundtrip FILE_OR_DIRECTORY...";
    }

    static byte[] exportReqif(List<Probe.Requirement> requirements, String requestedTimestamp) throws Exception {
        String timestamp = canonicalTimestamp(requestedTimestamp);
        List<Probe.Requirement> ordered = requirements.stream()
                .sorted(Comparator.comparing(Probe.Requirement::id))
                .toList();
        Document document = newDocument();
        Element root = element(document, document, "REQ-IF");
        root.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns", REQIF_NS);
        root.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:xhtml", XHTML_NS);

        Element headerContainer = element(document, root, "THE-HEADER");
        Element header = element(document, headerContainer, "REQ-IF-HEADER");
        header.setAttribute("IDENTIFIER", "MR_HEADER");
        textElement(document, header, "COMMENT", "Bounded export from mundane-req source; ReqIF metadata is transport-derived.");
        textElement(document, header, "CREATION-TIME", timestamp);
        textElement(document, header, "REQ-IF-TOOL-ID", PROFILE);
        textElement(document, header, "REQ-IF-VERSION", "1.0");
        textElement(document, header, "SOURCE-TOOL-ID", PROFILE);
        textElement(document, header, "TITLE", "mundanereq requirements exchange");

        Element coreContainer = element(document, root, "CORE-CONTENT");
        Element content = element(document, coreContainer, "REQ-IF-CONTENT");
        appendDatatypes(document, content, timestamp);
        appendSpecTypes(document, content, timestamp);

        Element objects = element(document, content, "SPEC-OBJECTS");
        for (Probe.Requirement requirement : ordered) appendRequirement(document, objects, requirement, timestamp);

        int relationshipCount = relationshipCount(ordered);
        if (relationshipCount > 0) {
            Element relations = element(document, content, "SPEC-RELATIONS");
            ordered.stream().forEach(requirement -> requirement.decomposes().stream()
                    .sorted()
                    .forEach(parent -> appendRelationship(document, relations, requirement.id(), parent, timestamp)));
        }

        appendSpecification(document, content, ordered, timestamp);
        return serialize(document);
    }

    private static void appendDatatypes(Document document, Element content, String timestamp) {
        Element datatypes = element(document, content, "DATATYPES");
        Element stringType = element(document, datatypes, "DATATYPE-DEFINITION-STRING");
        identifiable(stringType, DT_STRING, "mundanereq string", timestamp);
        stringType.setAttribute("MAX-LENGTH", "2147483647");
        Element xhtmlType = element(document, datatypes, "DATATYPE-DEFINITION-XHTML");
        identifiable(xhtmlType, DT_XHTML, "mundanereq rich content", timestamp);
    }

    private static void appendSpecTypes(Document document, Element content, String timestamp) {
        Element types = element(document, content, "SPEC-TYPES");
        Element objectType = element(document, types, "SPEC-OBJECT-TYPE");
        identifiable(objectType, TYPE_REQUIREMENT, "mundanereq requirement", timestamp);
        Element attributes = element(document, objectType, "SPEC-ATTRIBUTES");
        appendStringDefinition(document, attributes, AD_ID, "mundanereq.id", timestamp);
        appendStringDefinition(document, attributes, AD_TITLE, "mundanereq.title", timestamp);
        appendStringDefinition(document, attributes, AD_ALLOCATION, "mundanereq.allocation", timestamp);
        appendXhtmlDefinition(document, attributes, AD_STATEMENT, "mundanereq.statement", timestamp);
        appendXhtmlDefinition(document, attributes, AD_RATIONALE, "mundanereq.rationale", timestamp);
        appendStringDefinition(document, attributes, AD_SOURCE, "mundanereq.source", timestamp);

        Element relationType = element(document, types, "SPEC-RELATION-TYPE");
        identifiable(relationType, TYPE_DECOMPOSES, "mundanereq.decomposes", timestamp);
        Element specificationType = element(document, types, "SPECIFICATION-TYPE");
        identifiable(specificationType, TYPE_SPECIFICATION, "mundanereq derived flat specification", timestamp);
    }

    private static void appendStringDefinition(
            Document document, Element parent, String identifier, String name, String timestamp) {
        Element definition = element(document, parent, "ATTRIBUTE-DEFINITION-STRING");
        identifiable(definition, identifier, name, timestamp);
        Element type = element(document, definition, "TYPE");
        textElement(document, type, "DATATYPE-DEFINITION-STRING-REF", DT_STRING);
    }

    private static void appendXhtmlDefinition(
            Document document, Element parent, String identifier, String name, String timestamp) {
        Element definition = element(document, parent, "ATTRIBUTE-DEFINITION-XHTML");
        identifiable(definition, identifier, name, timestamp);
        Element type = element(document, definition, "TYPE");
        textElement(document, type, "DATATYPE-DEFINITION-XHTML-REF", DT_XHTML);
    }

    private static void appendRequirement(
            Document document, Element objects, Probe.Requirement requirement, String timestamp) {
        Element object = element(document, objects, "SPEC-OBJECT");
        identifiable(object, objectId(requirement.id()), requirement.title(), timestamp);
        Element values = element(document, object, "VALUES");
        appendStringValue(document, values, AD_ID, requirement.id());
        appendStringValue(document, values, AD_TITLE, requirement.title());
        if (requirement.allocation() != null) {
            appendStringValue(document, values, AD_ALLOCATION, requirement.allocation());
        }
        appendXhtmlValue(document, values, AD_STATEMENT, requirement.statement());
        if (requirement.rationale() != null) {
            appendXhtmlValue(document, values, AD_RATIONALE, requirement.rationale());
        }
        if (requirement.source() != null) appendStringValue(document, values, AD_SOURCE, requirement.source());
        Element type = element(document, object, "TYPE");
        textElement(document, type, "SPEC-OBJECT-TYPE-REF", TYPE_REQUIREMENT);
    }

    private static void appendStringValue(Document document, Element values, String definitionId, String value) {
        Element attribute = element(document, values, "ATTRIBUTE-VALUE-STRING");
        attribute.setAttribute("THE-VALUE", value);
        Element definition = element(document, attribute, "DEFINITION");
        textElement(document, definition, "ATTRIBUTE-DEFINITION-STRING-REF", definitionId);
    }

    private static void appendXhtmlValue(
            Document document, Element values, String definitionId, List<Probe.ContentBlock> blocks) {
        Element attribute = element(document, values, "ATTRIBUTE-VALUE-XHTML");
        Element definition = element(document, attribute, "DEFINITION");
        textElement(document, definition, "ATTRIBUTE-DEFINITION-XHTML-REF", definitionId);
        Element value = element(document, attribute, "THE-VALUE");
        Element container = xhtmlElement(document, value, "div");
        container.setAttribute("class", "mundanereq-blocks");
        for (Probe.ContentBlock block : blocks) {
            if (block instanceof Probe.ProseBlock prose) {
                Element paragraph = xhtmlElement(document, container, "p");
                paragraph.appendChild(document.createTextNode(prose.text()));
            } else if (block instanceof Probe.MathBlock math) {
                Element mathContainer = xhtmlElement(document, container, "div");
                mathContainer.setAttribute("class", "mundanereq-math-" + math.language());
                Element pre = xhtmlElement(document, mathContainer, "pre");
                pre.appendChild(document.createTextNode(math.payload()));
            }
        }
    }

    private static void appendRelationship(
            Document document, Element relations, String child, String parent, String timestamp) {
        Element relation = element(document, relations, "SPEC-RELATION");
        identifiable(relation, relationId(child, parent), "decomposes", timestamp);
        Element source = element(document, relation, "SOURCE");
        textElement(document, source, "SPEC-OBJECT-REF", objectId(child));
        Element target = element(document, relation, "TARGET");
        textElement(document, target, "SPEC-OBJECT-REF", objectId(parent));
        Element type = element(document, relation, "TYPE");
        textElement(document, type, "SPEC-RELATION-TYPE-REF", TYPE_DECOMPOSES);
    }

    private static void appendSpecification(
            Document document, Element content, List<Probe.Requirement> requirements, String timestamp) {
        Element specifications = element(document, content, "SPECIFICATIONS");
        Element specification = element(document, specifications, "SPECIFICATION");
        identifiable(specification, "MR_SPECIFICATION", "mundanereq derived flat specification", timestamp);
        Element children = element(document, specification, "CHILDREN");
        for (Probe.Requirement requirement : requirements) {
            Element hierarchy = element(document, children, "SPEC-HIERARCHY");
            identifiable(hierarchy, "MR_HIER_" + hex(requirement.id()), requirement.id(), timestamp);
            Element object = element(document, hierarchy, "OBJECT");
            textElement(document, object, "SPEC-OBJECT-REF", objectId(requirement.id()));
        }
        Element type = element(document, specification, "TYPE");
        textElement(document, type, "SPECIFICATION-TYPE-REF", TYPE_SPECIFICATION);
    }

    static List<Probe.Requirement> importReqif(byte[] xml, String sourceName) throws Exception {
        Document document = parse(xml);
        Element root = document.getDocumentElement();
        require(REQIF_NS.equals(root.getNamespaceURI()) && root.getLocalName().equals("REQ-IF"),
                "document root is not the ReqIF 1.2 schema REQ-IF element");
        Element header = requiredDescendant(root, "REQ-IF-HEADER");
        require(PROFILE.equals(requiredText(header, "REQ-IF-TOOL-ID")), "unsupported ReqIF tool profile");
        require(PROFILE.equals(requiredText(header, "SOURCE-TOOL-ID")), "unsupported ReqIF source profile");
        require("1.0".equals(requiredText(header, "REQ-IF-VERSION")), "unsupported ReqIF schema header version");

        Element content = requiredDescendant(root, "REQ-IF-CONTENT");
        Element objectsContainer = requiredChild(content, "SPEC-OBJECTS");
        List<Element> objects = childElements(objectsContainer, "SPEC-OBJECT");
        require(!objects.isEmpty(), "profile document contains no requirements");

        Map<String, ImportedRequirement> byObjectId = new HashMap<>();
        Map<String, ImportedRequirement> byHumanId = new HashMap<>();
        for (Element object : objects) {
            require(TYPE_REQUIREMENT.equals(requiredReference(object, "TYPE", "SPEC-OBJECT-TYPE-REF")),
                    "unsupported SPEC-OBJECT type");
            String transportId = requiredAttribute(object, "IDENTIFIER");
            Element values = requiredChild(object, "VALUES");
            Map<String, Object> fields = parseValues(values);
            require(fields.keySet().stream().allMatch(ReqifProbe::knownField), "unsupported requirement attribute");
            String id = requiredString(fields, AD_ID);
            String title = requiredString(fields, AD_TITLE);
            List<Probe.ContentBlock> statement = requiredBlocks(fields, AD_STATEMENT);
            ImportedRequirement imported = new ImportedRequirement(
                    id,
                    title,
                    optionalString(fields, AD_ALLOCATION),
                    statement,
                    optionalBlocks(fields, AD_RATIONALE),
                    optionalString(fields, AD_SOURCE));
            require(byObjectId.putIfAbsent(transportId, imported) == null, "duplicate ReqIF SPEC-OBJECT identifier");
            require(byHumanId.putIfAbsent(id, imported) == null, "duplicate mundane-req requirement ID");
            require(objectId(id).equals(transportId), "ReqIF transport identifier does not match mundane-req ID");
        }

        Element relationsContainer = optionalChild(content, "SPEC-RELATIONS");
        if (relationsContainer != null) {
            Set<String> relationshipKeys = new HashSet<>();
            for (Element relation : childElements(relationsContainer, "SPEC-RELATION")) {
                require(TYPE_DECOMPOSES.equals(requiredReference(relation, "TYPE", "SPEC-RELATION-TYPE-REF")),
                        "unsupported SPEC-RELATION type");
                require(optionalChild(relation, "VALUES") == null,
                        "profile decomposition relationships cannot carry attribute values");
                String childObject = requiredReference(relation, "SOURCE", "SPEC-OBJECT-REF");
                String parentObject = requiredReference(relation, "TARGET", "SPEC-OBJECT-REF");
                ImportedRequirement child = byObjectId.get(childObject);
                ImportedRequirement parent = byObjectId.get(parentObject);
                require(child != null && parent != null, "relationship references an unknown SPEC-OBJECT");
                String key = child.id + "\u0000" + parent.id;
                require(relationshipKeys.add(key), "duplicate decomposition relationship");
                child.decomposes.add(parent.id);
            }
        }

        verifySpecification(content, byObjectId.keySet());
        return byHumanId.values().stream()
                .sorted(Comparator.comparing(value -> value.id))
                .map(value -> value.toRequirement(sourceName))
                .toList();
    }

    private static Map<String, Object> parseValues(Element values) {
        Map<String, Object> fields = new HashMap<>();
        for (Element value : childElements(values, null)) {
            String localName = value.getLocalName();
            String definition;
            Object fieldValue;
            if (localName.equals("ATTRIBUTE-VALUE-STRING")) {
                definition = requiredReference(value, "DEFINITION", "ATTRIBUTE-DEFINITION-STRING-REF");
                fieldValue = requiredAttribute(value, "THE-VALUE");
            } else if (localName.equals("ATTRIBUTE-VALUE-XHTML")) {
                definition = requiredReference(value, "DEFINITION", "ATTRIBUTE-DEFINITION-XHTML-REF");
                fieldValue = parseBlocks(requiredChild(value, "THE-VALUE"));
            } else {
                throw new IllegalArgumentException("unsupported attribute value type " + localName);
            }
            require(fields.putIfAbsent(definition, fieldValue) == null, "duplicate requirement attribute " + definition);
        }
        return fields;
    }

    private static List<Probe.ContentBlock> parseBlocks(Element value) {
        List<Element> roots = childElementsNs(value, XHTML_NS, "div");
        require(roots.size() == 1, "XHTML value must contain one profile div");
        Element root = roots.getFirst();
        require("mundanereq-blocks".equals(root.getAttribute("class")), "XHTML value is outside the profile");
        List<Probe.ContentBlock> blocks = new ArrayList<>();
        for (Element block : childElementsNs(root, XHTML_NS, null)) {
            if (block.getLocalName().equals("p")) {
                require(!block.hasAttributes(), "profile prose paragraphs cannot carry attributes");
                blocks.add(new Probe.ProseBlock(block.getTextContent()));
            } else if (block.getLocalName().equals("div")
                    && block.getAttribute("class").equals("mundanereq-math-latex")) {
                List<Element> children = childElementsNs(block, XHTML_NS, "pre");
                require(children.size() == 1 && childElementsNs(block, XHTML_NS, null).size() == 1,
                        "profile math div must contain one pre element");
                blocks.add(new Probe.MathBlock("latex", children.getFirst().getTextContent()));
            } else {
                throw new IllegalArgumentException("unsupported XHTML block " + block.getLocalName());
            }
        }
        require(!blocks.isEmpty(), "XHTML field contains no semantic blocks");
        return List.copyOf(blocks);
    }

    private static void verifySpecification(Element content, Set<String> objectIds) {
        Element specifications = requiredChild(content, "SPECIFICATIONS");
        List<Element> values = childElements(specifications, "SPECIFICATION");
        require(values.size() == 1, "profile requires one derived specification");
        Element specification = values.getFirst();
        require(TYPE_SPECIFICATION.equals(requiredReference(specification, "TYPE", "SPECIFICATION-TYPE-REF")),
                "unsupported SPECIFICATION type");
        Element children = requiredChild(specification, "CHILDREN");
        Set<String> referenced = new HashSet<>();
        for (Element hierarchy : childElements(children, "SPEC-HIERARCHY")) {
            require(optionalChild(hierarchy, "CHILDREN") == null, "profile specification must be flat");
            String reference = requiredReference(hierarchy, "OBJECT", "SPEC-OBJECT-REF");
            require(referenced.add(reference), "SPEC-OBJECT occurs more than once in derived specification");
        }
        require(referenced.equals(objectIds), "derived specification does not reference every requirement exactly once");
    }

    private static boolean knownField(String field) {
        return Set.of(AD_ID, AD_TITLE, AD_ALLOCATION, AD_STATEMENT, AD_RATIONALE, AD_SOURCE).contains(field);
    }

    private static String requiredString(Map<String, Object> fields, String name) {
        Object value = fields.get(name);
        require(value instanceof String, "required string attribute is missing: " + name);
        return (String) value;
    }

    private static String optionalString(Map<String, Object> fields, String name) {
        Object value = fields.get(name);
        require(value == null || value instanceof String, "attribute has wrong value type: " + name);
        return (String) value;
    }

    @SuppressWarnings("unchecked")
    private static List<Probe.ContentBlock> requiredBlocks(Map<String, Object> fields, String name) {
        Object value = fields.get(name);
        require(value instanceof List<?>, "required XHTML attribute is missing: " + name);
        return (List<Probe.ContentBlock>) value;
    }

    @SuppressWarnings("unchecked")
    private static List<Probe.ContentBlock> optionalBlocks(Map<String, Object> fields, String name) {
        Object value = fields.get(name);
        require(value == null || value instanceof List<?>, "attribute has wrong value type: " + name);
        return (List<Probe.ContentBlock>) value;
    }

    private static Document newDocument() throws Exception {
        DocumentBuilderFactory factory = secureFactory();
        return factory.newDocumentBuilder().newDocument();
    }

    private static Document parse(byte[] xml) throws Exception {
        DocumentBuilderFactory factory = secureFactory();
        return factory.newDocumentBuilder().parse(new ByteArrayInputStream(xml));
    }

    private static DocumentBuilderFactory secureFactory() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        return factory;
    }

    private static byte[] serialize(Document document) throws Exception {
        TransformerFactory factory = TransformerFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        var transformer = factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(document), new StreamResult(output));
        return output.toByteArray();
    }

    private static Element element(Document document, Node parent, String name) {
        Element child = document.createElementNS(REQIF_NS, name);
        parent.appendChild(child);
        return child;
    }

    private static Element xhtmlElement(Document document, Node parent, String name) {
        Element child = document.createElementNS(XHTML_NS, "xhtml:" + name);
        parent.appendChild(child);
        return child;
    }

    private static void textElement(Document document, Node parent, String name, String value) {
        Element child = element(document, parent, name);
        child.appendChild(document.createTextNode(value));
    }

    private static void identifiable(Element element, String id, String name, String timestamp) {
        element.setAttribute("IDENTIFIER", id);
        element.setAttribute("LAST-CHANGE", timestamp);
        element.setAttribute("LONG-NAME", name);
    }

    private static Element requiredDescendant(Element parent, String name) {
        NodeList values = parent.getElementsByTagNameNS(REQIF_NS, name);
        require(values.getLength() == 1, "expected one " + name);
        return (Element) values.item(0);
    }

    private static Element requiredChild(Element parent, String name) {
        Element result = optionalChild(parent, name);
        require(result != null, "missing " + name);
        return result;
    }

    private static Element optionalChild(Element parent, String name) {
        Element result = null;
        for (Element child : childElements(parent, name)) {
            require(result == null, "duplicate " + name);
            result = child;
        }
        return result;
    }

    private static List<Element> childElements(Element parent, String localName) {
        return childElementsNs(parent, REQIF_NS, localName);
    }

    private static List<Element> childElementsNs(Element parent, String namespace, String localName) {
        List<Element> result = new ArrayList<>();
        for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof Element element
                    && namespace.equals(element.getNamespaceURI())
                    && (localName == null || localName.equals(element.getLocalName()))) {
                result.add(element);
            }
        }
        return List.copyOf(result);
    }

    private static String requiredText(Element parent, String name) {
        return requiredChild(parent, name).getTextContent();
    }

    private static String requiredReference(Element parent, String wrapper, String referenceName) {
        return requiredText(requiredChild(parent, wrapper), referenceName);
    }

    private static String requiredAttribute(Element element, String name) {
        require(element.hasAttribute(name), "missing attribute " + name + " on " + element.getLocalName());
        return element.getAttribute(name);
    }

    private static String canonicalTimestamp(String value) {
        try {
            return Instant.parse(value).toString();
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("timestamp must be an RFC 3339 UTC instant: " + value);
        }
    }

    private static String objectId(String requirementId) {
        return "MR_REQ_" + hex(requirementId);
    }

    private static String relationId(String sourceId, String targetId) {
        return "MR_REL_" + hex(sourceId) + "_" + hex(targetId);
    }

    private static String hex(String value) {
        StringBuilder result = new StringBuilder();
        for (byte item : value.getBytes(StandardCharsets.UTF_8)) {
            result.append("%02X".formatted(Byte.toUnsignedInt(item)));
        }
        return result.toString();
    }

    private static int relationshipCount(List<Probe.Requirement> requirements) {
        return requirements.stream().mapToInt(requirement -> requirement.decomposes().size()).sum();
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }

    private static final class ImportedRequirement {
        final String id;
        final String title;
        final String allocation;
        final List<Probe.ContentBlock> statement;
        final List<Probe.ContentBlock> rationale;
        final String source;
        final List<String> decomposes = new ArrayList<>();

        ImportedRequirement(
                String id,
                String title,
                String allocation,
                List<Probe.ContentBlock> statement,
                List<Probe.ContentBlock> rationale,
                String source) {
            this.id = id;
            this.title = title;
            this.allocation = allocation;
            this.statement = statement;
            this.rationale = rationale;
            this.source = source;
        }

        Probe.Requirement toRequirement(String sourceName) {
            List<String> parents = decomposes.stream().sorted().toList();
            return new Probe.Requirement(
                    id,
                    title,
                    allocation,
                    statement,
                    rationale,
                    source,
                    parents,
                    new Probe.Location(sourceName, 1, 1),
                    List.of());
        }
    }
}
