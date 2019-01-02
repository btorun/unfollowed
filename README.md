# unfollowed

## Introduction
_unfollowed_ runs on AWS Lambda and sends a direct message to authenticated user on Twitter if anyone unfollowed since the last run. It uses [Twitter API/OAuth](https://developer.twitter.com/en/docs/api-reference-index). You will need your own API tokens in order to run.

e.g. direct message
```text
Unfollowed by @jack, @biz
```

### Prerequisites
1. Twitter API access (free)
1. AWS account (free): Lambda functions & S3 storage
1. Java 8 or greater in order to build the project

## Setup
* Apply for developer access to Twitter API: https://developer.twitter.com
* Create S3 bucket: bucket name e.g. `twitter.yourdomain.com`
* Create Lambda function
   * Runtime: java8
   * Custom Policy for S3 write access
   ```json
    {
        "Version": "2012-10-17",
        "Statement": [
            {
                "Effect": "Allow",
                "Action": [
                    "s3:GetObject",
                    "s3:PutObject"
                ],
                "Resource": "arn:aws:s3:::twitter.yourdomain.com/*"
            }
        ]
    }
    ```
* Create following environment variables under function settings:

| Variable Name       | Value                   |
| ------------------- | ----------------------- |
| ACCESS_TOKEN        | `...`                   |
| ACCESS_TOKEN_SECRET | `...`                   |
| CONSUMER_API_KEY    | `...`                   |
| CONSUMER_API_SECRET | `...`                   |
| S3_BUCKET_NAME      | `twitter.yourdomain.com`|
| S3_FOLLOWERS_FILE   | `followers.json`        |
| S3_REGION           | `us-east-1`             |

* Set memory to `1024MB` and timeout to `20 secs`.
* [Build](#build) the code and upload to AWS Lambda.
* You can add _CloudWatch Events_ to run this function regularly.
e.g. `rate(20 minutes)`
   * Twitter API enforces rate limiting on standard api thus it's advised 
   to pick a rate greater than 15 minutes. Read more [below](#good-to-know).

### Build
1. Clone git repo
1. Build the code
```bash
$ ./gradlew build
```
You will find the zip file under `build/distributions`.

### Good to know
* Be mindful about Twitter API rate limiting. They limit [standard/free endpoints](https://developer.twitter.com/en/docs/basics/rate-limits). Read more about [rate limiting](https://developer.twitter.com/en/docs/basics/rate-limiting). If you have more than 75K followers, you will most likely hit the limit on a single run thus _unfollowed_ will not be able to fetch all followers.
* This app can easily operate within the limits of [AWS free tier (non-expiring) offerings](https://aws.amazon.com/free/). As of Jan 2019, 1M requests & 3.2M compute seconds are included in Lambda per month. S3 operating costs are negligible. (e.g. $0.00012)
* _unfollowed_ is designed to handle AWS Gateway API requests/responses out of the box. You can create a RESTful api and link with your Lambda function. When you hit the given api url, you will invoke _unfollowed_ function once and it will tell you about the unfollowers. e.g.


HTTP 200
```json
{
  "status":"ok",
  "message":"Unfollowed by [jack, biz]"
}
```

HTTP 304
```json
{
  "status":"ok",
  "message":"No change"
}
```

### Known Issues
- [ ] none so far

### Author
Baturalp Torun ([@bet3](https://twitter.com/bet3)) -- [https://baturalptorun.com](https://baturalptorun.com)
