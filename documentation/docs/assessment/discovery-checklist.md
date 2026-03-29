---
sidebar_position: 2
title: Discovery Checklist
---

# Discovery Phase Checklist

> Use this checklist during the initial discovery phase to gather all information needed to finalize the Quality Engineering Assessment.

## Product Inventory

For **each webapp product** in scope, complete the following:

### Product: _______________

- [ ] **Product Name & Version:**
- [ ] **URL(s):** Dev / Staging / Production
- [ ] **Frontend Framework:** React / Angular / Vue / Next.js / Other: ___
- [ ] **State Management:** Redux / NgRx / Vuex / Zustand / Other: ___
- [ ] **Component Library:** Material UI / Ant Design / Custom / Other: ___
- [ ] **Rendering Model:** CSR / SSR / SSG / Hybrid
- [ ] **Authentication Method:** OAuth2 / SAML / JWT / Session / SSO
- [ ] **API Type:** REST / GraphQL / gRPC-Web / WebSocket
- [ ] **Key User Roles:** (list roles that need test coverage)
- [ ] **Critical Business Flows:** (list top 5–10 workflows)
- [ ] **Known Pain Points:** (list testing challenges)
- [ ] **Existing Test Coverage:**
  - Manual test cases: ___
  - Automated tests: ___
  - Automation tool: ___
  - Pass rate: ___%
  - Flaky rate: ___%

---

## Team Assessment

- [ ] **Total QA team size:** ___
- [ ] **QA-to-Dev ratio:** ___
- [ ] **Team TypeScript experience (1–5):** ___
- [ ] **Team Playwright experience (1–5):** ___
- [ ] **Team automation experience (1–5):** ___
- [ ] **Current tools in use:** (list all testing tools)
- [ ] **Training budget available?** Yes / No
- [ ] **Dedicated automation engineers?** Yes / No — Count: ___

---

## SDLC & Process

- [ ] **Sprint cadence:** 1 week / 2 weeks / 3 weeks / Kanban
- [ ] **QA engagement point:** Requirements / Design / Development / Post-Dev
- [ ] **Test management tool:** Jira+Zephyr / TestRail / qTest / Other: ___
- [ ] **Defect tracking tool:** Jira / Azure Boards / Other: ___
- [ ] **CI/CD platform:** Jenkins / GitHub Actions / GitLab CI / Azure DevOps / Other: ___
- [ ] **Tests gated in CI?** Yes (blocking) / Yes (informational) / No
- [ ] **Deployment frequency:** Daily / Weekly / Bi-weekly / Monthly
- [ ] **Rollback strategy:** Blue-green / Canary / Manual / None

---

## Infrastructure & Environments

- [ ] **Cloud provider:** AWS / Azure / GCP / On-Prem / Hybrid
- [ ] **Test environments available:** Dev / QA / Staging / Pre-Prod / Prod
- [ ] **Environment provisioning:** On-demand / Shared / Static
- [ ] **Environment stability (1–5):** ___
- [ ] **Service virtualization in use?** Yes / No — Tool: ___
- [ ] **Docker available in CI?** Yes / No
- [ ] **Browser/device lab:** BrowserStack / Sauce Labs / Local / None

---

## Requirements & Compliance

- [ ] **Accessibility requirement:** WCAG 2.1 AA / Section 508 / None / Other: ___
- [ ] **Browser support matrix:**
  - Chrome: ___
  - Firefox: ___
  - Safari: ___
  - Edge: ___
  - Mobile browsers: ___
- [ ] **Performance requirements:** Yes / No — SLA: ___
- [ ] **Security testing requirements:** OWASP / Pen testing / None
- [ ] **Regulatory compliance:** SOX / HIPAA / PCI-DSS / GDPR / None

---

## Existing Automation Audit

> Complete this section only if an existing test automation framework exists.

- [ ] **Framework repository URL:** ___
- [ ] **Language / Runtime:** ___
- [ ] **Test framework:** Playwright / Cypress / Selenium / TestCafe / Other: ___
- [ ] **Architecture pattern:** POM / Screenplay / Custom / None
- [ ] **TypeScript strict mode?** Yes / No
- [ ] **Total test count:** ___
- [ ] **Test execution time (full suite):** ___ min
- [ ] **Parallel execution?** Yes / No — Workers: ___
- [ ] **Cross-browser execution?** Yes / No — Browsers: ___
- [ ] **Visual regression testing?** Yes / No — Tool: ___
- [ ] **Accessibility testing?** Yes / No — Tool: ___
- [ ] **API test layer?** Yes / No
- [ ] **Test data strategy:** Hardcoded / Fixtures / API-seeded / Factory / None
- [ ] **Reporting:** HTML / Allure / JUnit XML / Custom / None
- [ ] **CI pipeline integration?** Yes / No — Platform: ___
- [ ] **Documentation quality (1–5):** ___
- [ ] **Onboarding time for new team member:** ___ days
- [ ] **Last significant refactor date:** ___

---

## Scoring Summary

After completing the discovery, score each area (1–10):

| Area | Score | Notes |
|------|:-----:|-------|
| Architecture & Design | /10 | |
| Code Quality | /10 | |
| Test Stability | /10 | |
| CI/CD Integration | /10 | |
| Scalability | /10 | |
| Maintainability | /10 | |
| Reporting & Observability | /10 | |
| Cross-Browser/Device | /10 | |
| Test Data Management | /10 | |
| Reusability Across Products | /10 | |
| **OVERALL** | **/10** | |

> If the overall score is **≥ 7.0**, Option A (Enhance) is recommended.
> If the overall score is **< 7.0**, Option B (TAFlex-TS) or Option C (Custom) is recommended.
