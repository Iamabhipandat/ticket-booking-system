/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        cinema: {
          dark: '#0b0f19',
          card: '#131b2e',
          accent: '#e50914',
          gold: '#f59e0b',
        }
      }
    },
  },
  plugins: [],
}
