
import forge from "node-forge";

export const signResponse = (
  data: any,
  privateKeyPem: string | undefined,
  certPem: string | undefined,
) => {
  if (!privateKeyPem || !certPem) {
    console.warn("Signing keys not found. Returning unsigned data.");
    return data;
  }

  const body = JSON.stringify(data, Object.keys(data).sort());

  const md = forge.md.sha256.create();
  md.update(body, "utf8");
  const privateKey = forge.pki.privateKeyFromPem(privateKeyPem);
  const signature = privateKey.sign(md);

  return {
    data,
    signature: forge.util.encode64(signature),
    certificate: certPem,
  };
};
