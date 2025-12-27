import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

admin.initializeApp();
const db = admin.firestore();

export const updatePaymentStatus = functions.firestore
  .document("BankTransactions/{transactionId}")
  .onCreate(async (snap, context) => {
    const transaction = snap.data();
    if (transaction && transaction.status === "completed") {
      const paymentRef = db.collection("Payments").doc(transaction.userId);
      await paymentRef.set(
        { status: "paid" },
        { merge: true }
      );
    }
  });
