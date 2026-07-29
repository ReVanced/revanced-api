export function formatDatetime(isoString: string): string {
    return isoString
        .replace(/\.\d{3}Z$/, '')
        .replace(/Z$/, '')
        .replace(/[+-]\d{2}:\d{2}$/, '');
}
