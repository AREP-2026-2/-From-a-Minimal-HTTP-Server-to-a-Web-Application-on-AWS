"use strict";

/**
 * Asynchronous client for the four hardcoded services.
 *
 * Every action below:
 *  1. Prevents the default page navigation (form submit / link click).
 *  2. Builds the service URL from validated user input.
 *  3. Calls fetch() (the browser's async request API) so the page stays
 *     interactive while the response is pending.
 *  4. Shows a loading state, then checks response.ok BEFORE parsing JSON.
 *  5. Renders success into the "result" area, or a friendly message into
 *     the "error" area — network failures are handled separately from
 *     valid-but-erroneous HTTP responses.
 */

function setLoading(button, isLoading, loadingLabel) {
    if (isLoading) {
        button.dataset.originalLabel = button.textContent;
        button.textContent = loadingLabel;
        button.disabled = true;
    } else {
        button.textContent = button.dataset.originalLabel || button.textContent;
        button.disabled = false;
    }
}

function clear(el) {
    el.textContent = "";
}

/**
 * Calls a JSON service and routes the outcome to the right area.
 * Network failures (DNS, offline, connection refused) land in the catch
 * block; a reachable server returning 4xx/5xx is handled via response.ok.
 */
async function callService(url, { button, loadingLabel, resultEl, errorEl, onSuccess }) {
    clear(resultEl);
    clear(errorEl);
    setLoading(button, true, loadingLabel);

    try {
        const response = await fetch(url, { method: "GET" });

        let payload = null;
        try {
            payload = await response.json();
        } catch (parseError) {
            throw new Error("The server response was not valid JSON.");
        }

        if (!response.ok) {
            const message = (payload && payload.error) ? payload.error : `Request failed (HTTP ${response.status}).`;
            errorEl.textContent = message;
            return;
        }

        onSuccess(payload);
    } catch (networkError) {
        errorEl.textContent = "Network error: could not reach the server. " + networkError.message;
    } finally {
        setLoading(button, false, loadingLabel);
    }
}

document.getElementById("greet-form").addEventListener("submit", (event) => {
    event.preventDefault();
    const name = document.getElementById("greet-name").value.trim();
    const button = event.target.querySelector("button");
    const resultEl = document.getElementById("greet-result");
    const errorEl = document.getElementById("greet-error");

    if (!name) {
        clear(resultEl);
        errorEl.textContent = "Please enter a name.";
        return;
    }

    const url = "/api/greet?name=" + encodeURIComponent(name);
    callService(url, {
        button,
        loadingLabel: "Greeting…",
        resultEl,
        errorEl,
        onSuccess: (data) => {
            resultEl.textContent = data.message;
        },
    });
});

document.getElementById("square-form").addEventListener("submit", (event) => {
    event.preventDefault();
    const value = document.getElementById("square-value").value.trim();
    const button = event.target.querySelector("button");
    const resultEl = document.getElementById("square-result");
    const errorEl = document.getElementById("square-error");

    if (value === "" || Number.isNaN(Number(value))) {
        clear(resultEl);
        errorEl.textContent = "Please enter a valid number.";
        return;
    }

    const url = "/api/square?value=" + encodeURIComponent(value);
    callService(url, {
        button,
        loadingLabel: "Squaring…",
        resultEl,
        errorEl,
        onSuccess: (data) => {
            resultEl.textContent = `${data.input}^2 = ${data.result}`;
        },
    });
});

document.getElementById("time-button").addEventListener("click", (event) => {
    const button = event.target;
    const resultEl = document.getElementById("time-result");
    const errorEl = document.getElementById("time-error");

    callService("/api/time", {
        button,
        loadingLabel: "Asking…",
        resultEl,
        errorEl,
        onSuccess: (data) => {
            resultEl.textContent = `Server time: ${data.serverTimeIso}`;
        },
    });
});

document.getElementById("health-button").addEventListener("click", (event) => {
    const button = event.target;
    const resultEl = document.getElementById("health-result");
    const errorEl = document.getElementById("health-error");

    callService("/api/health", {
        button,
        loadingLabel: "Checking…",
        resultEl,
        errorEl,
        onSuccess: (data) => {
            resultEl.textContent = `Status: ${data.status}`;
        },
    });
});

document.getElementById("slow-button").addEventListener("click", (event) => {
    const button = event.target;
    const resultEl = document.getElementById("slow-result");
    const errorEl = document.getElementById("slow-error");

    callService("/api/time?delayMs=5000", {
        button,
        loadingLabel: "Waiting 5s on the server…",
        resultEl,
        errorEl,
        onSuccess: (data) => {
            resultEl.textContent = `Server time after the delay: ${data.serverTimeIso}`;
        },
    });
});
