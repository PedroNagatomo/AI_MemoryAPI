/// <reference types="node" />

import { AIMemoryClient } from "../dist/index";

console.log("🚀 Script iniciou");
const API_KEY = process.env.AIMEMORY_API_KEY ?? "amk_test_...";

async function main() {
  const client = new AIMemoryClient({
    apiKey: API_KEY,
    baseUrl: "http://localhost:8081",
  });

  console.log("🔹 Criando end-user...");
  const user = await client.endUsers.create({
    externalId: "user_001",
    metadata: { name: "Pedro" },
  });
  console.log("✅ End-user:", user.id);

  console.log("🔹 Ingerindo conversa...");
  const ingest = await client.memories.ingest({
    endUserId: user.id,
    sourceId: "conv_001",
    messages: [
      { role: "user", content: "Oi, sou Pedro e moro em São Paulo" },
      { role: "assistant", content: "Oi Pedro!" },
      { role: "user", content: "Trabalho como dev Java" },
    ],
  });
  console.log(
    `✅ ${ingest.extracted} memórias extraídas (${ingest.tokensUsed} tokens)`,
  );
  ingest.memories.forEach((m) =>
    console.log(`   - [${m.category}] ${m.content}`),
  );

  console.log('🔹 Buscando "São Paulo"...');
  const search = await client.memories.search({
    endUserId: user.id,
    query: "São Paulo",
  });
  console.log(`✅ ${search.count} resultados:`);
  search.memories.forEach((m) => console.log(`   - ${m.content}`));

  console.log("🔹 Consultando uso...");
  const usage = await client.usage.get();
  console.log(`✅ Plano ${usage.plan}:`);
  console.log(
    `   Memórias: ${usage.memoriesUsed}/${usage.memoriesQuota} (${(usage.memoriesPercent * 100).toFixed(1)}%)`,
  );
  console.log(
    `   Tokens:   ${usage.tokensUsed}/${usage.tokensQuota} (${(usage.tokensPercent * 100).toFixed(1)}%)`,
  );
  console.log(`   Reset em: ${usage.resetAt}`);
}

main().catch((err) => {
  console.error("❌ Erro:", err);
  process.exit(1);
});
