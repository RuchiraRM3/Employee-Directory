# Claude Code usage notes

## Prompts used (session 2026-11-09)
- "Act as a senior software engineer. Read implementation.md, task.md and the onboarding PDF, understand the system, improve the plan if needed, create a progress tracker, then start implementing."

## Suggestions accepted, changed or rejected
| Suggestion / situation | Decision | Why |
|---|---|---|
| Trim and lower-case the email in the **service** | **Changed.** A test failed because `@Email` rejected `" Rowan.Tate@Example.TEST "` before the service ever trimmed it. Normalization moved to the `EmployeeRequest` compact constructor, which runs before validation. | The test exposed a real bug: valid emails pasted with spaces were rejected. |
| Boot 3-era test imports (`org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc`) | **Rejected.** The class was located in the actual Boot 4.1.2 jars: `org.springframework.boot.webmvc.test.autoconfigure`. | Imports were checked against the real classpath instead of memory. |
| Spring Security for token checking | **Rejected** in favour of a plain `OncePerRequestFilter` with a `TokenVerifier` interface. | The only rule is "valid token required". Less code, simpler to explain, and easy to stub in tests. |
| Separate "wrong password" and "unknown user" messages (PDF) | **Changed.** Firebase docs (via Context7) show that both return `auth/invalid-credential` under Email Enumeration Protection. | Following the docs avoids implementing an impossible requirement; escalated to the mentor. |
| Setting `loading` with `setState` inside the search effect | **Changed** after oxlint `set-state-in-effect`. `loading` is now derived from "result key ≠ current query key". | No cascading renders, and it also covers Back/Forward navigation. |

## Process caveat
The API tests were written immediately after the implementation, not before it (test-first TDD was not strictly followed in this pass). The tests still caught one real defect (the email trimming above).
