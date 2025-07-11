package com.ibm.guardium.couchbase.capella;

public class GetClusterAuditLogExportResponse {
    public String auditLogDownloadURL;
    public transient String auditLogExportId;
    public transient String createdAt;
    public transient String start;
    public transient String end;
    public String status;
    public transient String expiration;
}
