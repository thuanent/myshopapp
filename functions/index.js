const functions = require("firebase-functions");
const axios = require("axios");
const crypto = require("crypto");
const {v4: uuidv4} = require("uuid"); // Library to generate unique IDs

// Need to install uuid library: cd functions && npm install uuid
// axios installed in a previous step: npm install axios

// ZaloPay Sandbox Configuration (Read from configured Environment Variables)
// You need to run the 'firebase functions:config:set' command to configure them
const ZALO_PAY_APP_ID = functions.config().zalopay.appid;
const ZALO_PAY_MERCHANT_KEY = functions.config().zalopay.merchant_key;

// ZaloPay Create Order Endpoint (Sandbox)
const ZALO_PAY_CREATE_ORDER_ENDPOINT = "https://sb-openapi.zalopay.vn/v2/create";

/**
 * Generates HMAC-SHA256 signature for ZaloPay API request.
 * @param {string} data - The data string to sign.
 * @return {string} The generated signature in hex format.
 */
function generateZaloPaySignature(data) {
  const hmac = crypto.createHmac("sha256", ZALO_PAY_MERCHANT_KEY);
  hmac.update(data);
  return hmac.digest("hex");
}

// Cloud Function to handle Callback/Webhook from ZaloPay (will write later)
// Placeholder for callback URL. You'll need to deploy the callback function
// first to get the actual URL.
// URL format: https://YOUR_REGION-YOUR_PROJECT_ID.cloudfunctions.net/handleZaloPayCallback
const ZALO_PAY_CALLBACK_URL = "YOUR_ZALOPAY_WEBHOOK_URL_PLACEHOLDER";


/**
 * Cloud Function to create a ZaloPay order.
 * Called from the Android client via HTTPS Callable function.
 * Receives order details, interacts with ZaloPay Sandbox API,
 * and returns the ZaloPay Order Token to the client.
 * @param {object} data - The data sent from the client.
 * Expected to contain orderId (string) and amount (number).
 * @param {object} context - The context of the function call
 * (contains authentication info).
 * @returns {Promise<object>} A promise that resolves with the ZaloPay
 * order token and other details on success, or rejects with an HttpsError
 * on failure.
 */
exports.createZaloPayOrder = functions.https.onCall(async (data, context) => {
  functions.logger.info("Received request to create ZaloPay order:", data);

  // Check if the request is authenticated
  // (user is logged in via Firebase Auth)
  // This ensures only logged-in users can attempt to create
  // a payment request.
  if (!context.auth) {
    functions.logger.warn("Unauthenticated request to create ZaloPay order.");
    throw new functions.https.HttpsError(
        "unauthenticated",
        "User must be authenticated to create an order.",
    );
  }

  // Get order information from the client (Android app)
  // `data` is expected to contain properties like orderId and amount.
  // Make sure the client sends all necessary information.
  // orderId could be the Firestore Order document ID you created earlier.
  const orderId = data.orderId;
  // amount is the total amount (integer, in VND)
  const amount = data.amount;
  const userId = context.auth.uid; // The UID of the calling user

  // Validate the received data from the client
  // ZaloPay requires a minimum amount of 1000 VND
  if (!orderId || typeof amount !== "number" || amount < 1000) {
    functions.logger.warn("Invalid order data received:", data);
    throw new functions.https.HttpsError(
        "invalid-argument",
        "Invalid order data provided (missing orderId or invalid amount)." +
        " Amount must be >= 1000 VND.",
    );
  }

  // The apptransid parameter must be unique within the day
  const orderTime = Date.now();
  // Generate a unique transaction ID for your application (unique per day)
  // Combine timestamp and a portion of a UUID
  const appTransId =
    `${orderTime}_${uuidv4().replace(/-/g, "").substring(0, 16)}`;

  const orderParams = {
    appid: ZALO_PAY_APP_ID,
    apptransid: appTransId,
    appuser: userId, // The user ID in your system (Firebase Auth UID)
    amount: amount, // Amount (integer)
    apptime: orderTime, // Request creation time (milliseconds)
    embeddata: JSON.stringify({ // Embedded data
      your_order_id: orderId, // Store your orderId here for later lookup
      user_id: userId,
      // Add other necessary information if needed
      // (e.g., detailed product list)
    }),
    item: JSON.stringify([]), // Product list (JSON string format)
    // Can send [] if no detail needed
    description: `Payment for order ${orderId}`, // Order description
    // bankcode: '', // Bank code (to open a specific payment interface)
    callback_url: ZALO_PAY_CALLBACK_URL, // URL of the Cloud Function
    // handling callbacks
  };

  // Create the data string to sign
  // (based on ZaloPay API "Create Order" documentation)
  // The order of parameters in the signature string is VERY IMPORTANT
  // and must follow the API documentation exactly!
  const dataToSign =
    `${orderParams.appid}|${orderParams.apptransid}|${orderParams.appuser}|` +
    `${orderParams.amount}|${orderParams.apptime}|${orderParams.embeddata}|` +
    `${orderParams.item}|${orderParams.callback_url}`;


  // Generate HMAC-SHA256 signature
  const signature = generateZaloPaySignature(dataToSign);

  // Add the signature to the final parameters sent to ZaloPay Server
  const requestParams = {
    ...orderParams, // Copy created parameters
    mac: signature, // Add the security signature
  };

  functions.logger.info("Request params sent to ZaloPay:", requestParams);

  try {
    // --- Send the Create Order request to ZaloPay Server (Sandbox) ---
    // ZaloPay v2 uses POST method and sends parameters in the body
    // (application/json)
    const response = await axios.post(
        ZALO_PAY_CREATE_ORDER_ENDPOINT,
        requestParams,
    );

    const zaloPayResponse = response.data; // Response from ZaloPay Server

    // Check the result from ZaloPay Server
    if (zaloPayResponse.returncode === 1) {
      // ZaloPay order creation successful, return Order Token and other info
      // to the Android app
      functions.logger.info("ZaloPay create order success:", zaloPayResponse);
      return {
        orderToken: zaloPayResponse.order_token,
        zpTransId: zaloPayResponse.zp_trans_id,
        appTransId: appTransId, // Your application's transaction ID
        returnCode: zaloPayResponse.returncode, // Result from ZaloPay Server
        returnMessage: zaloPayResponse.returnmessage,
      };
    } else {
      // ZaloPay Server returned an error (returncode other than 1)
      functions.logger.error("ZaloPay create order failed:", zaloPayResponse);
      // Throw an HttpsError to the Android app for handling
      throw new functions.https.HttpsError(
          "internal", // General error code
          zaloPayResponse.returnmessage || "Failed to create ZaloPay order.",
          zaloPayResponse, // Details of the error returned from ZaloPay
      );
    }
  } catch (error) {
    // Error during the ZaloPay API call
    // (e.g., network error, ZaloPay server error)
    functions.logger.error("Error calling ZaloPay API:", error);

    // Check if the error from Axios has a response from ZaloPay Server
    if (error.response && error.response.data) {
      functions.logger.error("ZaloPay API error response:",
          error.response.data);
      throw new functions.https.HttpsError(
          "internal",
          error.response.data.returnmessage || "Error calling ZaloPay API.",
          error.response.data,
      );
    } else {
      // Network error or other error without a ZaloPay response
      throw new functions.https.HttpsError(
          "internal",
          error.message || "Error calling ZaloPay API.",
      );
    }
  }
});

// TODO: Write Cloud Function to handle Callback/Webhook from ZaloPay here
// A newline at the end of the file to satisfy eol-last rule
