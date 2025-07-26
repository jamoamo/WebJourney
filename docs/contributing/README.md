# Contributing to WebJourney

We welcome contributions to the WebJourney project! Whether it's a bug report, a new feature, or an improvement to the documentation, your help is greatly appreciated. Please take a moment to review this guide before making your contribution.

## Table of Contents

1.  [How to Contribute](#how-to-contribute)
2.  [Reporting Bugs](#reporting-bugs)
3.  [Suggesting Enhancements](#suggesting-enhancements)
4.  [Setting Up Your Development Environment](#setting-up-your-development-environment)
5.  [Code Style](#code-style)
6.  [Running Tests](#running-tests)
7.  [Submitting Pull Requests](#submitting-pull-requests)

## 1. How to Contribute

There are several ways you can contribute to WebJourney:

*   **Report Bugs:** If you find a bug, please report it on our [GitHub Issues](https://github.com/jamoamo/webjourney/issues).
*   **Suggest Enhancements:** Have an idea for a new feature or improvement? Open an issue on GitHub to discuss it.
*   **Write Code:** Fix bugs, implement new features, or improve existing code.
*   **Improve Documentation:** Enhance our user guides, API docs, or examples.

## 2. Reporting Bugs

When reporting a bug, please include as much detail as possible to help us reproduce and fix it quickly:

*   **Description:** A clear and concise description of the bug.
*   **Steps to Reproduce:** Detailed steps to reliably reproduce the issue.
*   **Expected Behavior:** What you expected to happen.
*   **Actual Behavior:** What actually happened.
*   **Environment:** Your operating system, Java version, WebJourney version, Selenium version, and browser details.
*   **Screenshots/Logs:** (Optional) Attach relevant screenshots or log files.

## 3. Suggesting Enhancements

If you have an idea for an enhancement, please open a [GitHub Issue](https://github.com/jamoamo/webjourney/issues) to discuss it. This allows us to gather feedback and ensure that the enhancement aligns with the project's goals.

## 4. Setting Up Your Development Environment

To set up your local development environment, follow these steps:

### Prerequisites

*   **Java Development Kit (JDK) 21 or later:** [Download JDK](https://www.oracle.com/java/technologies/javase-downloads.html)
*   **Apache Maven:** [Download Maven](https://maven.apache.org/download.cgi)
*   **Git:** [Download Git](https://git-scm.com/downloads)
*   **An IDE (e.g., IntelliJ IDEA, VS Code):** Recommended for development.

### Steps

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/jamoamo/webjourney.git
    cd webjourney
    ```
2.  **Build the project:**
    ```bash
    mvn clean install
    ```
    This command compiles the project, runs tests, and installs the artifacts into your local Maven repository.

## 5. Code Style

WebJourney follows standard Java coding conventions. Please ensure your code adheres to:

*   **Google Java Format:** We recommend using the [Google Java Format](https://github.com/google/google-java-format) for consistent code formatting.
*   **Checkstyle:** The project uses Checkstyle to enforce coding standards. Ensure your code passes Checkstyle checks.

## 6. Running Tests

It is crucial to run tests before submitting a pull request to ensure your changes haven't introduced any regressions.

*   **Run all unit tests:**
    ```bash
    mvn test
    ```
*   **Run all integration tests:**
    ```bash
    mvn verify
    ```
*   **Run a specific test class:**
    ```bash
    mvn test -Dtest=MyTestClass
    ```

## 7. Submitting Pull Requests

1.  **Fork the repository** on GitHub.
2.  **Create a new branch** from `main` for your feature or bug fix:
    ```bash
    git checkout -b feature/your-feature-name
    ```
    or
    ```bash
    git checkout -b bugfix/your-bug-fix
    ```
3.  **Make your changes** and commit them with clear, concise commit messages.
4.  **Ensure all tests pass** (unit and integration) and Checkstyle checks are clear.
5.  **Push your branch** to your forked repository.
6.  **Open a Pull Request** against the `main` branch of the original WebJourney repository. 
    *   Provide a clear title and description of your changes.
    *   Reference any related issues (e.g., `Fixes #123`, `Closes #456`).

Thank you for your contribution! 
