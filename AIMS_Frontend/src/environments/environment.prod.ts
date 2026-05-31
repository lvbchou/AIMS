export const environment = {
  production: true,
  apiUrl: 'http://localhost:8080/api', // Or your production backend URL
  apiBaseUrl: 'http://localhost:8080/api',
  vietqr: {
    baseUrl: 'https://api.vietqr.io',
    clientId: 'YOUR_CLIENT_ID',
    clientSecret: 'YOUR_CLIENT_SECRET',
    bankCode: 'BIDV',
    accountNumber: '1234567890',
    accountName: 'YOUR_BUSINESS_NAME',
  },
  payment: {
    timeout: 300000,
    pollInterval: 5000
  }
};
