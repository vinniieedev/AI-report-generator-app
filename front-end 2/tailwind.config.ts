import type { Config } from "tailwindcss"

const config: Config = {
  darkMode: ["class"],
  content: [
    "./src/**/*.{js,jsx,ts,tsx}",
    "./public/index.html",
  ],
  theme: {
    extend: {
      borderRadius: {
        lg: "var(--radius)",
        md: "calc(var(--radius) - 2px)",
        sm: "calc(var(--radius) - 4px)",
      },
      colors: {
        background: "hsl(var(--background))",
        foreground: "hsl(var(--foreground))",
        card: {
          DEFAULT: "hsl(var(--card))",
          foreground: "hsl(var(--card-foreground))",
        },
        popover: {
          DEFAULT: "hsl(var(--popover))",
          foreground: "hsl(var(--popover-foreground))",
        },
        primary: {
          DEFAULT: "hsl(var(--primary))",
          foreground: "hsl(var(--primary-foreground))",
        },
        secondary: {
          DEFAULT: "hsl(var(--secondary))",
          foreground: "hsl(var(--secondary-foreground))",
        },
        muted: {
          DEFAULT: "hsl(var(--muted))",
          foreground: "hsl(var(--muted-foreground))",
        },
        accent: {
          DEFAULT: "hsl(var(--accent))",
          foreground: "hsl(var(--accent-foreground))",
        },
        destructive: {
          DEFAULT: "hsl(var(--destructive))",
          foreground: "hsl(var(--destructive-foreground))",
        },
        border: "hsl(var(--border))",
        input: "hsl(var(--input))",
        ring: "hsl(var(--ring))",
        chart: {
          1: "hsl(var(--chart-1))",
          2: "hsl(var(--chart-2))",
          3: "hsl(var(--chart-3))",
          4: "hsl(var(--chart-4))",
          5: "hsl(var(--chart-5))",
        },
		sky: {
          100: "#f0fbff",
          200: "#d6f4ff",
          300: "#b3ecff",
          400: "#7ddcff",
          500: "#4fcfff",
          600: "#2bb8eb",
          700: "#1596c6",
          800: "#0f6f94",
        },
        lavender: {
          100: "#f3f6ff",
          200: "#e3e9ff",
          300: "#ccd7ff",
          400: "#b6c9ff",
          500: "#9fb6ff",
          600: "#7f97f0",
          700: "#5f75cc",
          800: "#45559a",
        },
        rose: {
          100: "#fff1f6",
          200: "#ffdbe8",
          300: "#fbc8db",
          400: "#f7c4d7",
          500: "#f2a9c4",
          600: "#db7fa0",
          700: "#b85a7c",
          800: "#8f3e5b",
        },
        peach: {
          100: "#fff7ec",
          200: "#feeed9",
          300: "#fde8c7",
          400: "#fde2b8",
          500: "#f9cf95",
          600: "#e9b56e",
          700: "#c98f4a",
          800: "#9b6733",
        },
      },
      keyframes: {
        "accordion-down": {
          from: { height: "0" },
          to: { height: "var(--radix-accordion-content-height)" },
        },
        "accordion-up": {
          from: { height: "var(--radix-accordion-content-height)" },
          to: { height: "0" },
        },
      },
      animation: {
        "accordion-down": "accordion-down 0.2s ease-out",
        "accordion-up": "accordion-up 0.2s ease-out",
      },
    },
  },
  plugins: [require("tailwindcss-animate")],
}

export default config