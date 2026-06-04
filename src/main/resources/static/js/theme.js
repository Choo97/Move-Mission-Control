(() => {
    const storageKey = 'moveMissionTheme';
    const themes = [
        { value: 'sky', label: '하늘' },
        { value: 'coral', label: '코랄' },
    ];

    const params = new URLSearchParams(window.location.search);
    const requestedTheme = params.get('theme');
    const savedTheme = localStorage.getItem(storageKey);
    const initialTheme = themes.some((theme) => theme.value === requestedTheme)
        ? requestedTheme
        : savedTheme || 'sky';

    function applyTheme(theme) {
        if (theme === 'coral') {
            document.documentElement.dataset.theme = 'coral';
        } else {
            document.documentElement.removeAttribute('data-theme');
        }

        localStorage.setItem(storageKey, theme);

        document.querySelectorAll('[data-theme-option]').forEach((button) => {
            button.classList.toggle('is-active', button.dataset.themeOption === theme);
            button.setAttribute('aria-pressed', String(button.dataset.themeOption === theme));
        });
    }

    function createSwitcher() {
        const switcher = document.createElement('div');
        switcher.className = 'theme-switcher';
        switcher.setAttribute('aria-label', '색상 테마 선택');

        themes.forEach((theme) => {
            const button = document.createElement('button');
            button.type = 'button';
            button.textContent = theme.label;
            button.dataset.themeOption = theme.value;
            button.addEventListener('click', () => applyTheme(theme.value));
            switcher.appendChild(button);
        });

        document.body.appendChild(switcher);
        applyTheme(initialTheme);
    }

    applyTheme(initialTheme);

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', createSwitcher);
    } else {
        createSwitcher();
    }
})();
