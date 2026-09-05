package engineering.artifacts;

import java.util.Map;

public final class Problem extends RuntimeException {
    private static final long serialVersionUID=1L;
    public final String code;
    public final transient Map<String,Object> location;
    public Problem(String code,String message,String path) { this(code,message,Json.object("path",path,"line",1,"column",1)); }
    public Problem(String code,String message,Map<String,Object> location) { super(message);this.code=code;this.location=location; }
    public Map<String,Object> diagnostic() { return Json.object("code",code,"message",getMessage(),"location",location); }
    public boolean operational() { return code.equals("input-unavailable")||code.equals("input-changed"); }
}
