package wb.wolbu.entity;

public enum MemberType {
    STUDENT, INSTRUCTOR;

    public String getRole() {
        return "ROLE_" + this.name();
    }
}
