export const environment = {
  production: false,
  apiUrl: '/api',
  vietqr: {
    baseUrl: 'https://api.vietqr.io',
    clientId: 'customer-testaimvd-user26586',
    clientSecret: '',
    bankCode: 'BIDV',
    accountNumber: '8823302684',
    accountName: 'TESTAIMVD',
  },
  payment: {
    timeout: 300000,
    pollInterval: 5000
  }
};
