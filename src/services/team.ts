import { getBackend, getConfig } from '../config';
import type { Env } from '../types';

export async function getTeamMembers(env: Env) {
    const backend = getBackend(env);
    const { organization } = getConfig(env);

    const members = await backend.members(organization);

    return members.map((member) => ({
        name: member.name,
        avatar_url: member.avatarUrl,
        url: member.url,
        bio: member.bio,
        gpg_key: member.gpgKeys.ids[0]
            ? { id: member.gpgKeys.ids[0], url: member.gpgKeys.url }
            : null
    }));
}
