# Security Policy

## Supported Versions

Currently, the following versions of Kreeda-Ankana are supported with security updates:

| Version | Supported          |
| ------- | ------------------ |
| 1.0.x   | :white_check_mark: |
| < 1.0   | :x:                |

## Reporting a Vulnerability

We take the security of Kreeda-Ankana seriously. If you believe you have found a security vulnerability, please do NOT open a public issue. Instead, please report it through the following process:

1. **Email the maintainer**: Send an email to the project lead (Vaishnavi R) with a detailed description of the vulnerability.
2. **Include details**: Provide steps to reproduce, the potential impact, and any suggested fixes.
3. **Wait for response**: We will acknowledge your report within 48 hours and provide a timeline for a fix.

## Security Practices
- **Local Data Protection**: We use Room DB with safe data handling practices.
- **Cloud Security**: Firebase Firestore rules are used to restrict unauthorized access.
- **Sensitive Info**: We do NOT commit `google-services.json` or `local.properties` to the public repository.

---
*Stay safe and keep playing.*
