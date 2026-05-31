document.addEventListener("DOMContentLoaded", function () {
    const button = document.getElementById("themeToggle");
    const savedTheme = localStorage.getItem("theme");

    if (savedTheme === "dark") {
        document.documentElement.setAttribute("data-theme", "dark");
    } else {
        document.documentElement.removeAttribute("data-theme");
    }

    updateThemeButtonText(button);

    if (button) {
        button.addEventListener("click", function () {
            const currentTheme = document.documentElement.getAttribute("data-theme");

            if (currentTheme === "dark") {
                document.documentElement.removeAttribute("data-theme");
                localStorage.setItem("theme", "light");
            } else {
                document.documentElement.setAttribute("data-theme", "dark");
                localStorage.setItem("theme", "dark");
            }

            updateThemeButtonText(button);
        });
    }
});

function updateThemeButtonText(button) {
    if (!button) {
        return;
    }

    const currentTheme = document.documentElement.getAttribute("data-theme");

    if (currentTheme === "dark") {
        button.textContent = "Светлая тема";
    } else {
        button.textContent = "Тёмная тема";
    }
}