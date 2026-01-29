# AWS IAM Role Configuration Guide

This guide outlines how to configure an **IAM Role** with necessary **permission policies**, **trust relationships**, and **cross-account access** using the AWS Console. The role is intended to allow EC2 instances to access **Amazon SQS** and **Amazon S3** securely with least privilege.

---

## Purpose

Grant an EC2 instance the ability to:

- Read from specific **Amazon SQS queues**
- Access **CloudWatch logs** stored in **Amazon S3**
- Support cross-account usage (optional)

---

##  Prerequisites

- AWS account credentials with IAM privileges
- An existing **SQS queue**
- An existing **S3 bucket** with CloudWatch logs
- An EC2 instance to assume the role

---

## Instructions

### 1. Create IAM Role for EC2

1. Go to **IAM > Roles** in the [AWS Console](https://console.aws.amazon.com/iam/).
2. Click **Create role**.
3. **Select Trusted Entity Type**: Choose **AWS service**.
4. Select **EC2** as the use case.
5. Click **Next**.

---

### 2. Attach Policies

#### For Single Account Setup (EC2, SQS, and S3 in the same AWS account)

Click **Create policy** (in new tab), and create the following:

##### a. Amazon SQS Read-Only Access Policy

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "AmazonSQSReadOnlyAccess",
      "Effect": "Allow",
      "Action": [
        "sqs:GetQueueAttributes",
        "sqs:GetQueueUrl",
        "sqs:ListDeadLetterSourceQueues",
        "sqs:ListQueues",
        "sqs:ListMessageMoveTasks",
        "sqs:ListQueueTags"
      ],
      "Resource": "*"
    }
  ]
}
```

> Name this policy: `AmazonSQSReadOnlyCustom`

---

##### b. S3 Read-Only Access to CloudWatch Logs

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:GetObject"
      ],
      "Resource": [
        "arn:aws:s3:::<BUCKET_NAME>",
        "arn:aws:s3:::<BUCKET_NAME>/*"
      ]
    },
    {
      "Effect": "Allow",
      "Action": [
        "s3:ListBucket"
      ],
      "Resource": "arn:aws:s3:::<BUCKET_NAME>"
    }
  ]
}
```

> Replace `<BUCKET_NAME>` with your actual CloudWatch log bucket name in your current AWS account.

> Name this policy: `S3CloudWatchReadAccess`

---

##### c. SQS Full Access for Specific Queue

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "sqs:ReceiveMessage",
        "sqs:DeleteMessage",
        "sqs:GetQueueAttributes"
      ],
      "Resource": "arn:aws:sqs:<REGION>:<ACCOUNT_ID>:<QUEUE_NAME>"
    }
  ]
}
```

> Replace:
> - `<REGION>` with your AWS region (e.g., us-east-1)
> - `<ACCOUNT_ID>` with your current AWS account ID
> - `<QUEUE_NAME>` with your SQS queue name in your current AWS account

> Name this policy: `SQSQueueAccess`

---

##### d. Assume Role Permission (for same account)

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "VisualEditor0",
      "Effect": "Allow",
      "Action": "sts:AssumeRole",
      "Resource": [
        "arn:aws:iam::<ACCOUNT_ID>:role/<ROLE_NAME>/*",
        "arn:aws:iam::<ACCOUNT_ID>:role/<ROLE_NAME>",
        "arn:aws:sts::<ACCOUNT_ID>:assumed-role/<ROLE_NAME>/*",
        "arn:aws:sts::<ACCOUNT_ID>:assumed-role/<ROLE_NAME>/<EC2_INSTANCE_ID>"
      ]
    }
  ]
}
```

> Replace:
> - `<ACCOUNT_ID>` with your current AWS account ID
> - `<ROLE_NAME>` with the name of the role you're creating in your current AWS account
> - `<EC2_INSTANCE_ID>` with your EC2 instance ID in your current AWS account

> Name this policy: `AssumeRolePermission`

---

#### For Cross Account Setup (EC2 in one account, SQS/S3 in another)

### Step-by-Step Cross-Account Configuration

For this setup, we'll use the following terminology:
- **Account1**: The AWS account where your EC2 instance is hosted (e.g., Account ID: 111111)
- **Account2**: The AWS account where your SQS queue and S3 bucket are located (e.g., Account ID: 222222)

#### A. In Account1 (where EC2 instance is located)

1. Log in to your IAM console (https://console.aws.amazon.com/iam/) of Account1 where EC2 is hosted (e.g., Account ID: 111111)
2. Click the **Roles** tab under **Access Management**
3. Click the **Create Role** button
4. For **Trusted Entity Type**, select AWS Service
5. For **Use case**, select EC2
6. Click **Next**
7. Click **Create policy** (in new tab) to create the following policy:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "VisualEditor0",
      "Effect": "Allow",
      "Action": "sts:AssumeRole",
      "Resource": "arn:aws:iam::<Account2_ID>:role/<Role_In_Account2>"
    }
  ]
}
```

> Replace:
> - `<Account2_ID>` with the account ID where your SQS/S3 resources are located (e.g., 222222)
> - `<Role_In_Account2>` with the name of the role you'll create in Account2 (e.g., role_on_222222)

8. Name this policy (e.g., `CrossAccountAssumeRolePolicy`) and click **Create policy**
9. Return to the role creation tab, refresh the policy list, and attach the newly created policy
10. Click **Next**
11. Name the role (e.g., `role_on_111111`) and provide a description
12. Click **Create role**
13. In the IAM Role's **Trust relationships** tab, click **Edit trust policy**
14. Ensure the trust policy looks like this:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Service": "ec2.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
```

15. Click **Update Trust Policy**
16. Attach this role to your EC2 instance:
    1. Go to **EC2 > Instances**
    2. Select your instance > **Actions > Security > Modify IAM role**
    3. Choose the role you just created (e.g., `role_on_111111`) and click **Update IAM role**

#### B. In Account2 (where SQS/S3 resources are located)

1. Log in to your IAM console (https://console.aws.amazon.com/iam/) of Account2 where SQS/S3 resources are located (e.g., Account ID: 222222)
2. Click the **Roles** tab under **Access Management**
3. Click the **Create Role** button
4. For **Trusted Entity Type**, select **Another AWS account**
5. Enter the **Account ID of Account1** (e.g., 111111)
6. Click **Next**
7. Enter the role name (e.g., `role_on_222222`)
8. Click **Create Role**
9. Search for the created role (e.g., `role_on_222222`) and open it
10. Steps to set the Permissions Policies:
    1. In the **Permissions** tab, click the **Add Permissions** button and select **Create Inline Policy**
    2. On the **Create Policy** page, select JSON editor and add the following SQS policy:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "sqs:ReceiveMessage",
        "sqs:DeleteMessage",
        "sqs:DeleteMessageBatch",
        "sqs:ChangeMessageVisibility",
        "sqs:ChangeMessageVisibilityBatch",
        "sqs:GetQueueAttributes",
        "sqs:GetQueueUrl"
      ],
      "Resource": "arn:aws:sqs:<Region>:<Account2_ID>:<Queue_Name>"
    }
  ]
}
```

11. Click **Review Policy**
12. Enter the policy name (e.g., `SQSAccessPolicy`) and click **Create Policy**
13. In the **Permissions** tab, click the **Add Permissions** button and select **Create Inline Policy**
14. On the **Create Policy** page, select JSON editor and add the following S3 policy:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:GetObject"
      ],
      "Resource": [
        "arn:aws:s3:::<BUCKET_NAME>/AWSLogs/<Account2_ID>/*"
      ]
    },
    {
      "Effect": "Allow",
      "Action": [
        "s3:ListBucket"
      ],
      "Resource": "arn:aws:s3:::<BUCKET_NAME>",
      "Condition": {
        "StringLike": { "s3:prefix": ["AWSLogs/<Account2_ID>/*"] }
      }
    }
  ]
}
```

> Replace `<BUCKET_NAME>` with your actual CloudWatch log bucket name in Account2.

15. Click **Review Policy**
16. Enter the policy name (e.g., `S3AccessPolicy`) and click **Create Policy**
17. In the **Trust relationships** tab, click **Edit trust policy**
18. Replace the trust policy with:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "AWS": "arn:aws:iam::<Account1_ID>:role/<Role_In_Account1>"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
```

19. Click **Update Trust Policy**

---

#### C. Configure Role ARN in Logstash Input Plugin

In your Logstash configuration file, set the `role_arn` parameter to the ARN of the role in Account2:

```
input {
  s3sqs {
    # Other parameters...
    role_arn => "arn:aws:iam::<Account2_ID>:role/<Role_In_Account2>"   # e.g., "arn:aws:iam::222222:role/role_on_222222"
    # Additional parameters...
  }
}
```

> Replace `<Account2_ID>` and `<Role_In_Account2>` with your actual values.

---

### 3. Modify the Trust Relationship

#### For Single Account Setup

1. In the IAM Role's **Trust relationships** tab, click **Edit trust relationship**.
2. Replace the trust policy with:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Service": "ec2.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    },
    {
      "Sid": "AllowSelfAssume",
      "Effect": "Allow",
      "Principal": {
        "AWS": "arn:aws:iam::<ACCOUNT_ID>:role/<ROLE_NAME>"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
```

> Replace:
> - `<ACCOUNT_ID>` with your current AWS account ID
> - `<ROLE_NAME>` with the name of the role you're creating in your current AWS account

---

## Attach Role to EC2 Instance

1. Go to **EC2 > Instances**
2. Select your instance > **Actions > Security > Modify IAM role**
3. Choose the role `<ROLE_NAME>` and click **Update IAM role**
 
---

## References

- [AWS IAM User Guide: Tutorial - Delegate Access Across AWS Accounts Using IAM Roles](https://docs.aws.amazon.com/IAM/latest/UserGuide/tutorial_cross-account-with-roles.html)
- [AWS SQS Developer Guide: Cross-Account Permissions](https://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/sqs-customer-managed-policy-examples.html#grant-cross-account-permissions-to-role-and-user-name)
- [AWS S3 User Guide: Cross-Account Access](https://docs.aws.amazon.com/AmazonS3/latest/userguide/example-walkthroughs-managing-access-example2.html)
- [AWS STS Reference: AssumeRole](https://docs.aws.amazon.com/STS/latest/APIReference/API_AssumeRole.html)
