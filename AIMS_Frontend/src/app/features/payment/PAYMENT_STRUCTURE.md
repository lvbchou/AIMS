# Payment Feature — Cấu trúc File

> **Module:** `src/app/features/payment/`  
> **Mục đích:** Xử lý toàn bộ luồng thanh toán của AIMS, bao gồm VietQR, PayPal redirect, xác thực giao dịch và hiển thị kết quả.

---

## Tổng quan cây thư mục

```
src/app/features/payment/
├── models/
│   └── payment.model.ts
├── services/
│   ├── payment-mock.service.ts
│   └── vietqr-payment.service.ts
└── pages/
    ├── payment-vietqr/
    │   ├── payment-vietqr.component.ts
    │   ├── payment-vietqr.component.html
    │   ├── payment-vietqr.component.scss
    │   └── payment-vietqr.component.spec.ts
    ├── payment-redirect/
    │   ├── payment-redirect.component.ts
    │   ├── payment-redirect.component.html
    │   ├── payment-redirect.component.scss
    │   └── payment-redirect.component.spec.ts
    ├── payment-validating/
    │   ├── payment-validating.component.ts
    │   ├── payment-validating.component.html
    │   ├── payment-validating.component.scss
    │   └── payment-validating.component.spec.ts
    ├── payment-success/
    │   ├── payment-success.component.ts
    │   ├── payment-success.component.html
    │   ├── payment-success.component.scss
    │   └── payment-success.component.spec.ts
    └── payment-failed/
        ├── payment-failed.component.ts
        ├── payment-failed.component.html
        ├── payment-failed.component.scss
        └── payment-failed.component.spec.ts
```

---

## Luồng màn hình (Screen Flow)

```
[payment/vietqr] ──► [payment/redirect] ──► [payment/validating] ──► [payment/success]
                                                                   └──► [payment/failed]
```

| Route | Component | Mô tả |
|-------|-----------|-------|
| `/payment/vietqr` | `PaymentVietqrComponent` | Màn hình thanh toán chính — hiển thị QR code VietQR và thông tin thanh toán |
| `/payment/redirect` | `PaymentRedirectComponent` | Chuyển hướng đến PayPal — hiển thị trạng thái loading, tự redirect sau 3s |
| `/payment/validating` | `PaymentValidatingComponent` | Xác thực giao dịch — gọi API kiểm tra kết quả từ PayPal, hiển thị progress steps |
| `/payment/success` | `PaymentSuccessComponent` | Kết quả thành công — hiển thị Order Reference và chi tiết giao dịch |
| `/payment/failed` | `PaymentFailedComponent` | Kết quả thất bại — hiển thị mô tả lỗi và nút Retry Payment |

---

## Mô tả chi tiết từng file

### 📁 models/

#### `payment.model.ts`
Định nghĩa tất cả TypeScript interfaces và types dùng trong feature payment.

| Type / Interface | Mô tả |
|-----------------|-------|
| `PaymentMethod` | Union type: `'COD' \| 'BANK_TRANSFER' \| 'PAYPAL' \| 'VIETQR'` |
| `PaymentStatus` | Union type: `'PENDING' \| 'PROCESSING' \| 'SUCCESS' \| 'FAILED' \| 'COMPLETED'` |
| `OrderItem` | Thông tin một sản phẩm trong đơn hàng (id, title, type, price, quantity...) |
| `DeliveryInfo` | Thông tin giao hàng (tên, phone, địa chỉ, loại giao hàng) |
| `Order` | Đơn hàng đầy đủ gồm items, deliveryInfo, subtotal, VAT, total |
| `PaymentRequest` | Yêu cầu thanh toán gửi lên service (orderId, method, amount) |
| `PaymentResult` | Kết quả trả về sau thanh toán (transactionId, status, message, timestamp) |
| `PayPalRedirectInfo` | Thông tin redirect PayPal (paypalUrl, returnUrl, cancelUrl) |
| `VietQRPaymentInfo` | Thông tin thanh toán VietQR (orderId, provider, totalPayable) |
| `QRCodeResponse` | Response chứa dữ liệu QR code (qrCode base64/URL, accountName...) |
| `PaymentStatusResponse` | Trạng thái giao dịch từ API (transactionId, status, amount...) |
| `OrderConfirmationData` | Dữ liệu xác nhận đơn hàng sau thanh toán thành công |
| `PaymentFailureData` | Dữ liệu lỗi thanh toán (errorCode, errorMessage, orderId) |

---

### 📁 services/

#### `payment-mock.service.ts`
Service mock dùng trong **testing / development** — không gọi API thực.

- `processPayment(request: PaymentRequest): Observable<PaymentResult>` — Giả lập xử lý thanh toán, trả về Success/Failed ngẫu nhiên sau delay.
- `formatDateTime(date: Date): string` — Format ngày giờ theo định dạng `HH:mm:ss DD/MM/YYYY`.

#### `vietqr-payment.service.ts`
Service xử lý thanh toán **VietQR** — giao tiếp với API backend thực.

- `generateQRCode(info: VietQRPaymentInfo): Observable<QRCodeResponse>` — Gọi API để tạo QR code thanh toán.
- `checkPaymentStatus(transactionId: string): Observable<PaymentStatusResponse>` — Kiểm tra trạng thái giao dịch theo polling.
- `cancelPayment(transactionId: string): Observable<void>` — Huỷ giao dịch đang chờ.

---

### 📁 pages/

#### `payment-vietqr/` — Màn hình VietQR
**Layout:** Hai cột — Trái: QR code, phải: thông tin thanh toán + nút hành động.

| File | Vai trò |
|------|---------|
| `.ts` | Gọi `VietQRPaymentService.generateQRCode()`, polling trạng thái, điều hướng khi hoàn tất |
| `.html` | Step bar (Cart → Delivery → Payment), QR image, payment info panel |
| `.scss` | Layout hai cột, step bar styles, QR container, payment info card |

---

#### `payment-redirect/` — Màn hình Redirect PayPal
**Layout:** Căn giữa — icon lock với vòng xoay đỏ, tiêu đề lớn, nút huỷ.

| File | Vai trò |
|------|---------|
| `.ts` | Nhận `orderId` + `amount` từ router state, tự redirect sang `/payment/validating` sau 3 giây |
| `.html` | Icon box + spinner ring, tiêu đề "Redirecting to PayPal...", subtitle, nút Cancel Payment |
| `.scss` | Căn giữa full viewport, icon box với `border-top` spinner animation |

**State truyền vào (Router State):**
```ts
{ orderId: string, amount: number }
```

---

#### `payment-validating/` — Màn hình Xác thực
**Layout:** Hai cột — Trái: shield icon + tiêu đề, phải: card có viền đỏ trên + progress steps.

| File | Vai trò |
|------|---------|
| `.ts` | Gọi `PaymentMockService.processPayment()`, animate `step` (0→1→2→3), redirect sang success/failed |
| `.html` | Shield icon với lock badge, "Validating Payment Results...", 3 progress steps với trạng thái động |
| `.scss` | Layout hai cột, `border-top: 4px solid #A61C1C` trên card, step list với connector lines |

**Step state:**
| `step` | Nghĩa |
|--------|-------|
| `0` | Connecting to PayPal (đang chạy) |
| `1` | Verifying Transaction (đang chạy) |
| `2` | Finalizing Order (đang chạy) |
| `3` | Hoàn tất — redirect ngay |

---

#### `payment-success/` — Màn hình Thành công
**Layout:** Hai cột — Trái: checkmark icon + "Payment Successful" + subtitle, phải: card trắng + order reference + nút.

| File | Vai trò |
|------|---------|
| `.ts` | Nhận `PaymentResult` từ router state (fallback mock nếu navigate trực tiếp) |
| `.html` | Teal checkmark icon, tiêu đề, order reference block (viền đỏ trái), transaction details, View Invoice + Return to Home |
| `.scss` | Layout hai cột, teal color scheme (`#2E7D9B`), order reference với `border-left: 4px solid #A61C1C` |

**State truyền vào:**
```ts
{ result: PaymentResult }
```

---

#### `payment-failed/` — Màn hình Thất bại
**Layout:** Hai cột — Trái: error icon + "Payment / **Failed**" + gạch đỏ, phải: card với `border-top` đỏ + mô tả lỗi + nút.

| File | Vai trò |
|------|---------|
| `.ts` | Nhận `PaymentResult` từ router state (fallback mock nếu navigate trực tiếp) |
| `.html` | SVG error circle, tiêu đề hai dòng, error description section, transaction grid, Retry Payment + Return to Home |
| `.scss` | Layout hai cột, red color scheme (`#A61C1C`, `#8B1A1A`), `ERROR DESCRIPTION` label với dot đỏ |

**State truyền vào:**
```ts
{ result: PaymentResult }
```

---

## Màu sắc chủ đạo (Design Tokens)

| Token | Giá trị | Dùng trong |
|-------|---------|-----------|
| `--color-primary-red` | `#8B1A1A` | Buttons, borders chính |
| `--color-accent-red` | `#A61C1C` | Icon stroke, card top-border, dot |
| `--color-teal` | `#2E7D9B` | Success screen title, icon, button |
| `--color-bg` | `#F5F5F5` | Background toàn bộ payment pages |
| `--color-card` | `#FFFFFF` | Nền card |
| `--color-text-main` | `#1A1A2E` | Tiêu đề, value text |
| `--color-text-muted` | `#555` / `#888` | Subtitle, label text |

---

## Ghi chú kỹ thuật

- **Standalone Components:** Tất cả components đều dùng `standalone: true`, import `CommonModule` trực tiếp.
- **Router State:** Data được truyền giữa các màn hình qua `router.navigate([], { state: {...} })` và đọc trong constructor qua `this.router.getCurrentNavigation()?.extras?.state`.
- **Mock Fallback:** Mọi component đều có hardcoded mock data trong `ngOnInit` để test trực tiếp bằng URL mà không cần đi qua flow đầy đủ.
- **Font:** `Inter` (Google Fonts) — được import trong `src/index.html`.
- **Icons:** Google Material Icons — được import trong `src/index.html`.
