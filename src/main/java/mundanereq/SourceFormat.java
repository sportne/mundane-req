package mundanereq;

/** Explicit authoring contracts; never inferred from parse failure. */
public enum SourceFormat {
    CUSTOM_02("custom-0.2", "mundanereq-source-0.2", ".mreq"),
    YAML_03("yaml-0.3", "mundanereq-yaml-0.3", ".mreq.yaml");

    public final String option;
    public final String contract;
    public final String suffix;

    SourceFormat(String option, String contract, String suffix) {
        this.option = option;
        this.contract = contract;
        this.suffix = suffix;
    }
}
