import { OpenAPIHono } from '@hono/zod-openapi';
import { swaggerUI } from '@hono/swagger-ui';
import type { Env } from './types';
import { cacheControl, CacheDuration } from './cache';
import { getConfig } from './config';
import packageJson from '../package.json';
import patchesApp from './routes/patches';
import managerApp from './routes/manager';
import announcementsApp from './routes/announcements';
import contributorsApp from './routes/contributors';
import teamApp from './routes/team';
import aboutApp from './routes/about';

type AppBindings = { Bindings: Env };

let _app: OpenAPIHono<AppBindings> | undefined;

export default {
    async fetch(
        request: Request,
        env: Env,
        ctx: ExecutionContext
    ): Promise<Response> {
        if (!_app) {
            const { apiVersion } = getConfig(env);

            _app = new OpenAPIHono<AppBindings>();

            _app.onError((err, c) => {
                console.error(err);
                return c.json(
                    {
                        error: err.message || 'Unknown error',
                        stack: err.stack
                    },
                    500
                );
            });

            // Default 5-minute cache for all routes (overridden per-route where needed)
            _app.use('*', cacheControl(CacheDuration.short));

            const versionedApp = new OpenAPIHono<AppBindings>();
            versionedApp.route('/patches', patchesApp);
            versionedApp.route('/manager', managerApp);
            versionedApp.route('/announcements', announcementsApp);
            versionedApp.route('/contributors', contributorsApp);
            versionedApp.route('/team', teamApp);
            versionedApp.route('/about', aboutApp);

            _app.route(`/v${apiVersion}`, versionedApp);
            _app.get('/', swaggerUI({ url: `/v${apiVersion}/openapi` }));

            _app.doc(`/v${apiVersion}/openapi`, () => ({
                openapi: '3.1.0',
                info: {
                    title: 'ReVanced API',
                    version: packageJson.version,
                    description: 'API server for ReVanced.',
                    contact: {
                        name: 'ReVanced',
                        url: 'https://revanced.app',
                        email: 'contact@revanced.app'
                    },
                    license: {
                        name: 'AGPLv3',
                        url: 'https://github.com/ReVanced/revanced-api/blob/main/LICENSE'
                    }
                },
                servers: [
                    {
                        url: 'https://api.revanced.app',
                        description: 'Production'
                    },
                    {
                        url: '{customServer}',
                        description: 'Custom server',
                        variables: {
                            customServer: {
                                default: 'api.revanced.app',
                                description: 'Custom server URL'
                            }
                        }
                    }
                ]
            }));
            _app.openAPIRegistry.registerComponent(
                'securitySchemes',
                'Bearer',
                {
                    type: 'http',
                    scheme: 'bearer'
                }
            );
        }
        return _app.fetch(request, env, ctx);
    }
};
