# Security

This section covers Imposter security. Topics include transport layer security (i.e. HTTPS) and authentication.

There are two primary approaches for adding TLS and authentication:

1. Using Imposter's embedded HTTP server
2. Using a reverse proxy or load balancer in front of Imposter

This section covers the first approach - using the embedded HTTP server. Using a reverse proxy or load balancer is a larger topic outside the scope of this documentation.

## TLS/SSL

You can run Imposter with HTTPS enabled. To do this, enable the TLS option and provide keystore options.

[Read more about how to enable TLS/SSL](./tls_ssl.md).

## Authentication

Imposter can require specific header values to authenticate incoming HTTP requests. To do this, use the `security` section within the plugin configuration file.

> Note: this example uses the [OpenAPI plugin](./openapi_plugin.md) but the same configuration works with [other plugins](./index.md) as well.

```yaml
# example-config.yaml
---
plugin: openapi
specFile: petstore.yaml

security:
  # no requests permitted by default
  default: Deny

  # only requests meeting these conditions are permitted
  conditions:
  - effect: Permit
    requestHeaders:
      Authorization: s3cr3t
```

### Concepts and terminology

Authentication configuration uses the following terms:

| Term      | Meaning                                                                     | Examples                           |
|-----------|-----------------------------------------------------------------------------|------------------------------------|
| Condition | A property of the request, such as the presence of a specific header value. | `Authorization` header value `foo` |
| Operator  | How the condition is matched.                                               | `EqualTo`, `NotEqualTo`            |
| Effect    | The impact of the condition on the request, such as it being denied.        | `Permit`, `Deny`                   |

The first important concept is the _Default Effect_. This is the effect that applies to all requests in the absence of a more specific condition. It is good practice to adhere the principle of least privilege. You can achieve this by setting the default effect to `Deny`, and then adding specific conditions that permit access.

```yaml
security:
  # no requests permitted by default
  default: Deny
```

This configuration will cause all responses from Imposter to have an `HTTP 401 Unauthorized` status.

Once you have configured the default effect, you typically add _Conditions_ to your security configuration, optionally specifying an _Operator_.

```yaml
security:
  # no requests permitted by default
  default: Deny

  # only requests meeting these conditions are permitted
  conditions:
  - effect: Permit
    requestHeaders:
      Authorization: s3cr3t
```

In this example, Imposter only permits requests that have the following HTTP request header:

```
Authorization: s3cr3t
```

Imposter will respond to these requests as normal, but respond to those without this specific header value with `HTTP 401 Unauthorized` status.  

The header name and value is arbitrary - you do not have to use the `Authorization` header. For example, you could specify:

```yaml
conditions:
- effect: Permit
  requestHeaders:
    X-Custom-Api-Key: s3cr3t
```

### Logical operations

The presence of more than one header in a condition is a logical 'AND' - in other words, both header values must match in order for a request to be permitted.

```yaml
conditions:
- effect: Permit
  requestHeaders:
    X-Custom-Api-Key: s3cr3t
    X-Another-Example: someothervalue
```

If you wish to model a logical 'OR', then use two conditions, as follows:

```yaml
# requests are permitted if either (1) or (2) is present
conditions:
# (1) either this header must be present...
- effect: Permit
  requestHeaders:
    X-Custom-Api-Key: s3cr3t

# (2) ...or this header must be present
- effect: Permit
  requestHeaders:
    X-Another-Example: someothervalue
```

### Match operators

By default, conditions are matched using the `EqualTo` operator. If you want to control this, you can use this form of configuration:

```yaml
conditions:
- effect: Deny
  requestHeaders:
    Authorization:
      value: s3cr3t
      operator: NotEqualTo
```

Here, the `value` of the `Authorization` header is specified as a child property. The `operator` can also be specified in this form, such as `EqualTo` or `NotEqualTo`. 

### More examples

See the `docs/examples` directory for working sample configurations, such as:

* [Simple authentication](./examples/openapi/authentication-simple)
* [Extended authentication](./examples/openapi/authentication-extended)