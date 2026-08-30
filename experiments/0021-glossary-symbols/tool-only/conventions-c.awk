# In this candidate, authoritative meaning and accepted spellings live in tool
# implementation rather than requirements-project source.
BEGIN {
    meaning = "The state declared by SYS-006 when its configured loss condition is met and retained until vehicle reactivation."
}
!/^#/ && /command link/ && /(unavailable|lost)/ {
    observed++
    if (index($0, "command link lost") || index($0, "command link is declared lost")) {
        recognized++
    } else {
        print display ":" FNR ": tool-convention: unrecognized command-link state" > "/dev/stderr"
        failures++
    }
}
END {
    print "tool_owned_meaning=" meaning
    print "recognized_uses=" (recognized + 0)
    exit failures ? 3 : 0
}
