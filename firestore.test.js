const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

// 1. Unauthenticated checks
test("Unauthenticated user: cannot read user profile", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
});

test("Unauthenticated user: cannot write attendance record", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("records").doc("2026-08-17_period_1").set({
      userId: ALICE_UID,
      date: "2026-08-17",
      period: 1,
      status: "PRESENT",
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

// 2. Cross-user isolation
test("Bob cannot read or write Alice's records", async () => {
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("records").get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("records").doc("2026-08-17_period_1").set({
      userId: ALICE_UID,
      date: "2026-08-17",
      period: 1,
      status: "PRESENT",
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

// 3. Alice can manage her own profile and records
test("Alice can create and read her profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      studentName: "Bhawishay",
      college: "Indian Institute of Handloom Technology, Varanasi",
      branch: "Handloom and Textile Technology",
      semester: "5th Semester",
      academicYear: "2026-27",
      targetPercentage: 75,
      selectedBatch: "Batch A",
      themeMode: "system",
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());
});

test("Alice can create and query her attendance records", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("records").doc("2026-08-17_period_1").set({
      userId: ALICE_UID,
      date: "2026-08-17",
      period: 1,
      subjectCode: "BHT503",
      subjectName: "Quality Evaluation of Fibers and Yarns",
      status: "PRESENT",
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("records").get()
  );
});

test("Invalid status or invalid period is rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  // Invalid status "DOG"
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("records").doc("2026-08-17_period_1").set({
      userId: ALICE_UID,
      date: "2026-08-17",
      period: 1,
      status: "INVALID_STATUS",
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
  // Invalid period 9
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("records").doc("2026-08-17_period_9").set({
      userId: ALICE_UID,
      date: "2026-08-17",
      period: 9,
      status: "PRESENT",
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});
