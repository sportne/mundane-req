#!/usr/bin/env ruby
# frozen_string_literal: true

require "csv"
require "digest"
require "fileutils"
require "json"
require "open3"
require "tmpdir"

abort "usage: compare.rb VALIDATOR OUTPUT_DIRECTORY" unless ARGV.length == 2

root = File.expand_path("../..", __dir__)
validator = File.expand_path(ARGV[0])
output = File.expand_path(ARGV[1])
abort "validator is not executable: #{validator}" unless File.executable?(validator)
FileUtils.mkdir_p(output)

expected_sources = {
  "fixtures/0.1/valid/" => "fixtures/0.1/valid/requirements.mreq",
  "fixtures/0.1/invalid/dangling-reference.mreq" => "fixtures/0.1/invalid/dangling-reference.mreq",
  "fixtures/0.1/invalid/duplicate-id/" => "fixtures/0.1/invalid/duplicate-id/two.mreq",
  "fixtures/0.1/invalid/duplicate-relationship.mreq" => "fixtures/0.1/invalid/duplicate-relationship.mreq",
  "fixtures/0.1/invalid/missing-statement.mreq" => "fixtures/0.1/invalid/missing-statement.mreq",
  "fixtures/0.1/invalid/unknown-field.mreq" => "fixtures/0.1/invalid/unknown-field.mreq",
  "fixtures/0.1/invalid/unterminated-math.mreq" => "fixtures/0.1/invalid/unterminated-math.mreq",
  "fixtures/0.2/valid/" => "fixtures/0.2/valid/requirements.mreq",
  "fixtures/0.2/invalid/comment-before-body.mreq" => "fixtures/0.2/invalid/comment-before-body.mreq",
  "fixtures/0.2/invalid/comment-only.mreq" => "fixtures/0.2/invalid/comment-only.mreq",
  "fixtures/0.2/invalid/comment-splits-math.mreq" => "fixtures/0.2/invalid/comment-splits-math.mreq",
  "fixtures/0.2/invalid/comment-splits-prose.mreq" => "fixtures/0.2/invalid/comment-splits-prose.mreq",
  "fixtures/0.2/invalid/leading-non-ascii-whitespace.mreq" => "fixtures/0.2/invalid/leading-non-ascii-whitespace.mreq",
  "fixtures/0.2/invalid/prohibited-c1-control.mreq" => "fixtures/0.2/invalid/prohibited-c1-control.mreq",
  "fixtures/0.2/invalid/supplementary-scalar-column.mreq" => "fixtures/0.2/invalid/supplementary-scalar-column.mreq",
  "fixtures/0.2/invalid/trailing-non-ascii-whitespace.mreq" => "fixtures/0.2/invalid/trailing-non-ascii-whitespace.mreq"
}.freeze

allowed_anchor_choices = {
  "fixtures/0.1/invalid/duplicate-relationship.mreq" => %w[6 13 6 1],
  "fixtures/0.1/invalid/unterminated-math.mreq" => %w[8 1 6 3],
  "fixtures/0.2/invalid/comment-before-body.mreq" => %w[4 1 3 1],
  "fixtures/0.2/invalid/comment-splits-math.mreq" => %w[6 1 4 3],
  "fixtures/0.2/invalid/comment-splits-prose.mreq" => %w[5 1 6 1]
}.freeze

checksum_out, checksum_err, checksum_status = Open3.capture3(
  "sha256sum", "-c", "result/SHA256SUMS", chdir: __dir__
)
abort "frozen result checksum failure:\n#{checksum_out}#{checksum_err}" unless checksum_status.success?
expected_result_files = %w[
  result/SHA256SUMS
  result/fixture-results.tsv
  result/method.md
  result/standard-findings.md
  result/valid-0.1.inventory
  result/valid-0.2.inventory
].freeze
actual_result_files = Dir.glob(File.join(__dir__, "result", "**", "*"))
                         .select { |path| File.file?(path) }
                         .map { |path| path.delete_prefix(__dir__ + "/") }
                         .sort
abort "unexpected or missing frozen result files" unless actual_result_files == expected_result_files.sort
manifest_entries = File.readlines(File.join(__dir__, "result", "SHA256SUMS"), chomp: true)
                       .map { |line| line.split(/\s+/, 2).fetch(1) }
                       .sort
abort "result manifest coverage is incomplete or duplicated" unless manifest_entries == (expected_result_files - ["result/SHA256SUMS"]).sort
Dir.mktmpdir("mundanereq-independent-package-", "/tmp") do |directory|
  package = File.join(directory, "package")
  package_out, package_err, package_status = Open3.capture3(
    File.join(__dir__, "prepare-package.sh"), package
  )
  abort "input package verification failure:\n#{package_out}#{package_err}" unless package_status.success?
end

def read_json_string(text)
  JSON.parse(text)
end

def assign_once(requirement, field, value)
  abort "duplicate independent inventory field #{field} in #{requirement['id']}" if requirement.key?(field)
  requirement[field] = value
end

def parse_independent(path)
  lines = File.readlines(path, chomp: true)
  abort "missing contract header in #{path}" unless lines.shift == 'contract "mundanereq-source-0.2"'
  requirements = []
  index = 0
  index += 1 while index < lines.length && lines[index].empty?
  while index < lines.length
    break if lines[index] == "relationships ["
    abort "unknown top-level inventory line: #{lines[index]}" unless lines[index].start_with?("requirement ")
    requirement = {"id" => read_json_string(lines[index].delete_prefix("requirement "))}
    index += 1
    until lines[index] == "end requirement"
      line = lines[index]
      case line
      when /^title (.+)$/
        assign_once(requirement, "title", read_json_string(Regexp.last_match(1)))
      when /^allocation (.+)$/
        assign_once(requirement, "allocation", Regexp.last_match(1) == "null" ? nil : read_json_string(Regexp.last_match(1)))
      when /^source (.+)$/
        assign_once(requirement, "source", Regexp.last_match(1) == "null" ? nil : read_json_string(Regexp.last_match(1)))
      when /^decomposition-targets (.+)$/
        targets = JSON.parse(Regexp.last_match(1))
        abort "decomposition targets are not sorted and unique" unless targets == targets.sort.uniq
        assign_once(requirement, "decomposes", targets)
      when /^(statement|rationale) null$/
        assign_once(requirement, Regexp.last_match(1), nil)
      when /^(statement|rationale) \[$/
        field = Regexp.last_match(1)
        blocks = []
        index += 1
        until lines[index] == "]"
          block = lines[index].strip
          if block.start_with?("prose ")
            blocks << ["prose", read_json_string(block.delete_prefix("prose "))]
          elsif block.start_with?("math latex ")
            blocks << ["math:latex", read_json_string(block.delete_prefix("math latex "))]
          else
            abort "unknown independent block: #{block}"
          end
          index += 1
        end
        assign_once(requirement, field, blocks)
      when ""
        # Blank lines are permitted only as presentation between fields/records.
      else
        abort "unknown independent requirement line: #{line}"
      end
      index += 1
    end
    required = %w[id title allocation statement rationale source decomposes]
    abort "incomplete independent requirement #{requirement['id']}" unless requirement.keys.sort == required.sort
    requirements << requirement
    index += 1
    index += 1 while index < lines.length && lines[index].empty?
  end
  abort "missing relationships section in #{path}" unless lines[index] == "relationships ["
  index += 1
  relationships = []
  until lines[index] == "]"
    match = lines[index]&.match(/^  ("(?:[^"\\]|\\.)*") -> ("(?:[^"\\]|\\.)*")$/)
    abort "unknown relationship inventory line: #{lines[index]}" unless match
    relationships << [read_json_string(match[1]), read_json_string(match[2])]
    index += 1
  end
  index += 1
  index += 1 while index < lines.length && lines[index].empty?
  abort "trailing independent inventory content: #{lines[index]}" unless index == lines.length
  abort "duplicate independent requirement ID" unless requirements.map { |item| item["id"] }.uniq.length == requirements.length
  identifiers = requirements.map { |requirement| requirement.fetch("id") }
  abort "independent requirements are not in declared deterministic order" unless identifiers == identifiers.sort
  abort "explicit relationships are not sorted and unique" unless relationships == relationships.sort.uniq
  derived = requirements.flat_map do |requirement|
    requirement.fetch("decomposes").map { |target| [requirement.fetch("id"), target] }
  end.sort
  abort "explicit relationship section disagrees with requirement targets" unless relationships == derived
  requirements
end

def expected_value(text)
  text.gsub("\\\\", "\\")
end

def parse_expected(path)
  requirements = []
  current = nil
  File.foreach(path, chomp: true) do |line|
    case line
    when /^requirement (.+)$/
      current = {"id" => Regexp.last_match(1), "statement" => [], "decomposes" => []}
    when /^title (.*)$/
      current["title"] = expected_value(Regexp.last_match(1))
    when /^allocation absent$/
      current["allocation"] = nil
    when /^allocation value (.*)$/
      current["allocation"] = expected_value(Regexp.last_match(1))
    when /^statement prose (.*)$/
      current["statement"] << ["prose", expected_value(Regexp.last_match(1))]
    when /^statement math ([^ ]+) (.*)$/
      current["statement"] << ["math:#{Regexp.last_match(1)}", expected_value(Regexp.last_match(2))]
    when /^rationale null$/
      current["rationale"] = nil
    when /^rationale prose (.*)$/
      (current["rationale"] ||= []) << ["prose", expected_value(Regexp.last_match(1))]
    when /^source absent$/
      current["source"] = nil
    when /^source value (.*)$/
      current["source"] = expected_value(Regexp.last_match(1))
    when /^decomposes (.+)$/
      current["decomposes"] << Regexp.last_match(1)
    when "end requirement"
      current["decomposes"].sort!
      requirements << current
      current = nil
    end
  end
  requirements.sort_by { |requirement| requirement.fetch("id") }
end

semantic_rows = []
["0.1", "0.2"].each do |version|
  independent = parse_independent(File.join(__dir__, "result", "valid-#{version}.inventory"))
  expected = parse_expected(File.join(root, "conformance", version, "valid", "expected.inventory"))
  abort "semantic disagreement for #{version}" unless independent == expected
  canonical = JSON.generate(independent.map do |requirement|
    %w[id title allocation statement rationale source decomposes]
      .to_h { |field| [field, requirement.fetch(field)] }
  end)
  semantic_rows << [version, independent.length, Digest::SHA256.hexdigest(canonical)]
end

participant_rows = CSV.read(
  File.join(__dir__, "result", "fixture-results.tsv"),
  headers: true,
  col_sep: "\t"
)
expected_headers = %w[selection verdict decisive_clause source_path line column reason]
abort "unexpected participant TSV headers" unless participant_rows.headers == expected_headers
selections = participant_rows.map { |row| row.fetch("selection") }
abort "participant selection set is incomplete, duplicated, or unexpected" unless selections.sort == expected_sources.keys.sort

maintained_rows = []
comparison_rows = []
participant_rows.each do |row|
  selection = row.fetch("selection")
  abort "unexpected participant source path for #{selection}" unless row.fetch("source_path") == expected_sources.fetch(selection)
  abort "missing decisive clause for #{selection}" if row.fetch("decisive_clause").strip.empty?
  abort "missing reason for #{selection}" if row.fetch("reason").strip.empty?
  unless %w[accepted rejected].include?(row.fetch("verdict"))
    abort "unexpected participant verdict for #{selection}"
  end
  if row.fetch("verdict") == "accepted"
    abort "accepted selection has a failure coordinate: #{selection}" unless row.fetch("line") == "-" && row.fetch("column") == "-"
  else
    abort "invalid participant coordinate for #{selection}" unless row.fetch("line").match?(/\A[1-9][0-9]*\z/) && row.fetch("column").match?(/\A[1-9][0-9]*\z/)
  end
  repository_selection = row.fetch("selection").sub(%r{\Afixtures/}, "conformance/")
  stdout, stderr, status = Open3.capture3(validator, File.join(root, repository_selection))
  verdict = status.exitstatus.zero? ? "accepted" : "rejected"
  abort "unexpected validator status #{status.exitstatus} for #{repository_selection}" unless [0, 1].include?(status.exitstatus)
  diagnostic = stderr.lines.first&.chomp
  if diagnostic
    match = diagnostic.match(/\A(.+):(\d+):(\d+): ([^:]+):/)
    abort "unparsed diagnostic: #{diagnostic}" unless match
    diagnostic_path = match[1]
    line = match[2]
    column = match[3]
    code = match[4]
    expected_diagnostic_path = File.join(root, expected_sources.fetch(selection).sub(%r{\Afixtures/}, "conformance/"))
    abort "maintained diagnostic names wrong source for #{selection}" unless diagnostic_path == expected_diagnostic_path
  else
    diagnostic_path = line = column = code = "-"
  end
  maintained_rows << [row.fetch("selection"), verdict, status.exitstatus, diagnostic_path, line, column, code, stdout.chomp, diagnostic || "-"]
  abort "verdict disagreement for #{row.fetch('selection')}" unless verdict == row.fetch("verdict")
  coordinate_tuple = [row.fetch("line"), row.fetch("column"), line, column]
  coordinate = if verdict == "accepted" || coordinate_tuple[0, 2] == coordinate_tuple[2, 2]
                 "equal"
               elsif allowed_anchor_choices[selection] == coordinate_tuple
                 "permitted diagnostic-anchor choice"
               else
                 abort "unreviewed coordinate disagreement for #{selection}: #{coordinate_tuple}"
               end
  comparison_rows << [row.fetch("selection"), "equal", row.fetch("line"), row.fetch("column"), line, column, coordinate]
end

CSV.open(File.join(output, "maintained-results.tsv"), "w", col_sep: "\t") do |csv|
  csv << %w[selection verdict status source_path line column diagnostic_code stdout first_diagnostic]
  maintained_rows.each { |row| csv << row }
end
CSV.open(File.join(output, "comparison.tsv"), "w", col_sep: "\t") do |csv|
  csv << %w[selection verdict participant_line participant_column maintained_line maintained_column disposition]
  comparison_rows.each { |row| csv << row }
end
CSV.open(File.join(output, "semantic-comparison.tsv"), "w", col_sep: "\t") do |csv|
  csv << %w[fixture_set requirements canonical_semantic_sha256 disposition]
  semantic_rows.each { |row| csv << [*row, "equal"] }
end

puts "PASS #{participant_rows.length} independent verdicts and both complete semantic inventories agree"
