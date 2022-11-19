public class TagMessage {

    String[] message_type = {"feat", "fix", "chore", "refactor", "docs", "style", "test", "ci", "build", "revert"};
    private int commit_id;
    private int type_id;
    private String tag;
    private String author_id;
    private String project_id;
    private String type;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private String description;

    public int getCommit_id() {
        return commit_id;
    }

    public void setCommit_id(int commit_id) {
        this.commit_id = commit_id;
    }

    public int getType_id() {
        return type_id;
    }

    public void setType_id(int type_id) {
        this.type_id = type_id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getAuthor_id() {
        return author_id;
    }

    public void setAuthor_id(String author_id) {
        this.author_id = author_id;
    }

    public String getProject_id() {
        return project_id;
    }

    public void setProject_id(String project_id) {
        this.project_id = project_id;
    }

    public void selectType(){
        this.type = message_type[this.type_id];
    }


    public String setMessage() {
        selectType();
        return this.type + ": " + this.description;
    }




}
