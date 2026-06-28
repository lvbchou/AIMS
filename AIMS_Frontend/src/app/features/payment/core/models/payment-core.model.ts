// =============================================
// payment.model.ts
// Models for Pay Order use case
// =============================================

export type PaymentMethod = 'COD' | 'BANK_TRANSFER' | 'PAYPAL' | 'VIETQR';
export type PaymentStatus = 'PENDING' | 'PROCESSING' | 'SUCCESS' | 'FAILED' | 'COMPLETED';

export interface OrderItem {
  id: string;
  title: string;
  type: 'CD' | 'DVD' | 'Book' | 'LP';
  price: number;        // VND
  quantity: number;
  imageUrl: string;
  weight: number;       // kg
}

export interface DeliveryInfo {
  recipientName: string;
  phone: string;        // 10-digit
  email: string;
  province: string;
  address: string;
  deliveryType: 'STANDARD' | 'RUSH';
  shippingFee: number;  // VND
}

export interface Order {
  id: string;
  items: OrderItem[];
  deliveryInfo: DeliveryInfo;
  subtotal: number;     // VND
  vatAmount: number;    // 10% VAT
  total: number;        // VND
}

export interface PaymentRequest {
  orderId: string;
  method: PaymentMethod;
  amount: number;
}

export interface PaymentResult {
  transactionId: string;
  status: PaymentStatus;
  message: string;
  timestamp: Date;
  orderId: string;
}

export interface PayPalRedirectInfo {
  paypalUrl: string;
  returnUrl: string;
  cancelUrl: string;
  orderId: string;
}

// =============================================
// VietQR Payment Models
// =============================================

export interface Invoice {
  orderId: string;
  totalPayable: number;
  customerName: string;
  customerEmail: string;
  items: OrderItem[];
  paymentMethod: PaymentMethod;
}

export interface VietQRPaymentInfo {
  orderId: string;
  provider: string; // 'VietQR'
  totalPayable: number; // in VND
  description: string;
  expirationTime?: number; // in seconds
}

// Unified QR Code Response (works with both mock and real API)
export interface QRCodeResponse {
  qrCode: string; // QR image (base64, URL, or SVG)
  qrDataUrl?: string; // Alternative URL format
  vietQrReference?: string; // Reference for VietQR
  transactionId?: string; // VietQR transaction ID
  amount?: number; // Payment amount
  orderId?: string; // Order ID
  content?: string; // Nội dung chuyển tiền (dùng cho Test Callback VietQR)
  accountName?: string; // Merchant account name
  accountNumber?: string; // Merchant account
  paymentInfo?: VietQRPaymentInfo; // For compatibility
}

// Legacy VietQRCodeResponse (for backwards compatibility)
export interface VietQRCodeResponse extends QRCodeResponse {
  qrData: string; // raw QR data
  paymentInfo: VietQRPaymentInfo;
}

// Payment Status Response
export interface PaymentStatusResponse {
  transactionId: string;
  orderId: string;
  status: 'COMPLETED' | 'PENDING' | 'FAILED';
  amount: number;
  timestamp: string;
  payerBankCode?: string;
  payerAccountName?: string;
  payerAccountNumber?: string;
  message: string;
}

// Order Confirmation Data
export interface OrderConfirmationData {
  customerName: string;
  phoneNumber: string;
  shippingAddress: string;
  province: string;
  totalAmountToBePaid: number; // in VND
  transactionId: string;
  transactionContent: string;
  transactionDatetimeDisplay: string; // format: DD/MM/YYYY HH:mm:ss
  orderName: string;
}

// Payment Failure Data
export interface PaymentFailureData {
  errorCode: string;
  errorMessage: string;
  orderId: string;
}
