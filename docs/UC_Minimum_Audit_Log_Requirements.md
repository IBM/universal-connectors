# Minimum Native Audit Log Requirements for Universal Connector (UC) Support

This document defines the minimum audit log capabilities a database vendor must provide to enable integration with the Universal Connector framework.

## Overview

Meeting these requirements allows:

- **Activity monitoring**
- **Policy enforcement**
- **Security alerting**
- **SIEM forwarding**

## Core Requirements

To enable Universal Connector integration, native audit logs must minimally provide:

- **Who** executed activity
- **What** was executed
- **Where** it was executed
- **From where** it originated
- **When** it occurred
- **Under which** session

### Capabilities Enabled

With these elements, UC can:

- Detect misuse
- Enforce policy
- Generate alerts
- Integrate with SIEM platforms

> **Important:** Without these fields, UC cannot reliably parse and analyze database activity.

---

## Minimum Required Fields

### Identity

- **Database User ID** – Who executed the activity

### Activity

- **Executed SQL / Command** – Full SQL text (Must not be truncated)
- **Command Type** – SELECT, INSERT, UPDATE, DELETE, DDL, ADMIN
- **Execution Result** – Success / Failure (error code if available)

### Object Context

- **Database Name** – Target database
- **Tables Accessed** – Directly logged OR inferable from SQL

### Source Context

- **Client IP or Hostname** – Where the query originated
- **Database Server IP or Hostname** – Where query was executed

### Time

- **Execution Timestamp** – When activity occurred

### Session Context

- **Session ID / Connection ID** – Strongly recommended for activity correlation

---

## Strongly Recommended (Optional but Valuable)

The following fields enhance UC capabilities:

- **OS User** – Attribution
- **Client Program** – Application-level policies
- **Records Affected** – Behavioral analytics
- **Effective Role** – Privilege monitoring

---

## Log Format Requirements

### Format Standards

Audit logs must be:

- **Structured** (JSON preferred)
- **UTF-8 encoded**
- **Consistently formatted**

### Access Methods

Logs must be accessible via at least one of:

- File export
- Streaming
- API
- Read-only audit table

---

## Event Types to Support

The following event types should be captured:

- **Login / Logout**
- **Query Execution**
- **DML Activity** (INSERT/UPDATE/DELETE)
- **DDL Activity** (CREATE/ALTER/DROP)
- **Privileged / Admin Commands**
- **Failed Queries**
- **Audit Configuration Changes**

---

## Security Use Cases to Support

The audit logs should enable monitoring for:

- **Failed SQL Commands**
- **Confidential Data Access**
- **Data Modification Activity**
- **Administrative Actions**
- **Audit Tampering**

---

## Compliance Notes

Ensuring these minimum requirements are met allows organizations to:

- Meet regulatory compliance requirements
- Implement effective security monitoring
- Detect and respond to security incidents
- Maintain audit trails for forensic analysis