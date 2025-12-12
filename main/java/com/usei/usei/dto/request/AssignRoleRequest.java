// com.usei.usei.dto.request.AssignRoleRequest.java
package com.usei.usei.dto.request;

public class AssignRoleRequest {
    private Long roleId;        // opcional
    private String roleName;    // opcional

    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
}
