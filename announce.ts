import { basename } from "node:path";
import type { ExtensionAPI } from "@earendil-works/pi-coding-agent";

export default function (pi: ExtensionAPI) {

let startedAt: number | undefined;

pi.on("agent_start", () => {
  // Keep the original start across automatic retries.
  startedAt ??= Date.now();
});

pi.on("agent_settled", async (event, ctx) => {
  const elapsed = startedAt === undefined ? 0 : Date.now() - startedAt;
  startedAt = undefined;

  if (event.aborted || elapsed < 30_000) return;
  if (process.platform !== "darwin") return;

  let location = "work tree:" + basename(ctx.cwd);

  try {
    const result = await pi.exec(
      "git",
      ["branch", "--show-current"],
      { cwd: ctx.cwd, timeout: 2000 },
    );

    const branch = result.stdout.trim();
    if (result.code === 0 && branch) {
      location += `, branch: ${branch}`;
    }
  } catch {
    // Still announce if Git isn't available.
  }
  await pi.exec("/usr/bin/say", [
    `${location}.`,
    ]);
  });
}
