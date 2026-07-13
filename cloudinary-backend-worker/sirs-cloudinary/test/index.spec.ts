import { env, createExecutionContext, waitOnExecutionContext } from "cloudflare:test";
import { describe, it, expect } from "vitest";
import worker from "../src/index";

const IncomingRequest = Request<unknown, IncomingRequestCfProperties>;

const testEnv = {
	...env,
	CLOUDINARY_CLOUD_NAME: "demo",
	CLOUDINARY_API_KEY: "key",
	CLOUDINARY_API_SECRET: "secret",
	DELETE_SECRET: "delete-secret",
};

async function callWorker(request: Request): Promise<Response> {
	const ctx = createExecutionContext();
	const response = await worker.fetch(request, testEnv, ctx);
	await waitOnExecutionContext(ctx);
	return response;
}

describe("Cloudinary delete worker", () => {
	it("rejects non-POST requests", async () => {
		const response = await callWorker(new IncomingRequest("http://example.com"));
		expect(response.status).toBe(405);
	});

	it("rejects invalid delete secret", async () => {
		const response = await callWorker(
			new IncomingRequest("http://example.com", {
				method: "POST",
				body: JSON.stringify({ publicId: "sirs/test", secret: "wrong" }),
				headers: { "Content-Type": "application/json" },
			}),
		);
		expect(response.status).toBe(403);
	});

	it("requires publicId", async () => {
		const response = await callWorker(
			new IncomingRequest("http://example.com", {
				method: "POST",
				body: JSON.stringify({ secret: "delete-secret" }),
				headers: { "Content-Type": "application/json" },
			}),
		);
		expect(response.status).toBe(400);
	});
});
