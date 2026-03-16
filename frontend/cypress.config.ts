import { defineConfig } from "cypress";

// Type declaration for require in Cypress Node context
declare const require: (id: string) => any;

export default defineConfig({
  e2e: {
    baseUrl: "http://localhost:5173",

    setupNodeEvents(on, config) {
      // Use require for code coverage (works in Cypress Node context)
      // eslint-disable-next-line @typescript-eslint/no-require-imports
      require('@cypress/code-coverage/task')(on, config);
      return config;
    },
  },
});
