/** @type {import('tailwindcss').Config} */
export default {
  darkMode: "class",
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        surface: "rgb(var(--color-surface) / <alpha-value>)",
        canvas: "rgb(var(--color-canvas) / <alpha-value>)",
        border: "rgb(var(--color-border) / <alpha-value>)",
        muted: "rgb(var(--color-muted) / <alpha-value>)",
        subtle: "rgb(var(--color-subtle) / <alpha-value>)",
        body: "rgb(var(--color-body) / <alpha-value>)",
        heading: "rgb(var(--color-heading) / <alpha-value>)",
        brand: {
          DEFAULT: "rgb(var(--color-brand) / <alpha-value>)",
          hover: "rgb(var(--color-brand-hover) / <alpha-value>)",
          light: "rgb(var(--color-brand-light) / <alpha-value>)",
        },
        accent: {
          DEFAULT: "rgb(var(--color-accent) / <alpha-value>)",
          light: "rgb(var(--color-accent-light) / <alpha-value>)",
          hover: "rgb(var(--color-accent-hover) / <alpha-value>)",
        },
        success: {
          DEFAULT: "rgb(var(--color-success) / <alpha-value>)",
          light: "rgb(var(--color-success-light) / <alpha-value>)",
        },
      },
      fontFamily: {
        sans: ["Inter", "ui-sans-serif", "system-ui", "-apple-system", "sans-serif"],
        serif: ["'Lora'", "Georgia", "serif"],
      },
      boxShadow: {
        card: "0 1px 3px rgba(0,0,0,0.05), 0 1px 2px rgba(0,0,0,0.04)",
        "card-hover": "0 8px 24px rgba(0,0,0,0.10), 0 2px 6px rgba(0,0,0,0.05)",
        nav: "0 1px 0 rgba(0,0,0,0.07)",
        modal: "0 25px 60px rgba(0,0,0,0.25), 0 8px 20px rgba(0,0,0,0.12)",
      },
      borderRadius: {
        card: "14px",
      },
      backgroundImage: {
        "gradient-brand": "linear-gradient(135deg, #4f46e5 0%, #8b5cf6 100%)",
        "gradient-accent": "linear-gradient(135deg, #8b5cf6 0%, #d946ef 100%)",
        "gradient-hero": "linear-gradient(135deg, #f5f3ff 0%, #eff6ff 50%, #ffffff 100%)",
      },
    },
  },
  plugins: [],
};
