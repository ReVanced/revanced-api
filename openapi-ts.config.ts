import { defineConfig } from '@hey-api/openapi-ts';

export default defineConfig({
    input: 'placeholder.yaml', // the input is overridden by the CLI
    output: {
        clean: false,
        path: 'client/ts/generated'
    }
});
