package it.dedagroup.panelapp.log.bean;

public class PanelappLogPortletBundle {
    private String id;

    private String state;

    private String level;

    private String name;

    private String version;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getState() {
        return this.state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getLevel() {
        return this.level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String toString() {
        return "Bundle [id=" + this.id + ", state=" + this.state + ", level=" + this.level + ", name=" + this.name + ", version=" + this.version + "]";
    }

    public PanelappLogPortletBundle(String id, String state, String level, String name, String version) {
        this.id = id;
        this.state = state;
        this.level = level;
        this.name = name;
        this.version = version;
    }

    public PanelappLogPortletBundle() {}
}
