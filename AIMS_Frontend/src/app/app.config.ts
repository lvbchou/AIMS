import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';

import { routes } from './app.routes';

import { authInterceptor } from './core/interceptors/auth-interceptor';

import { PRODUCT_FORM_FACTORIES } from './features/product/services/forms/product-form-factory.token';

import { DvdFormFactory } from './features/product/services/forms/dvd-form-factory.service';
import { CdFormFactory } from './features/product/services/forms/cd-form-factory.service';
import { BookFormFactory } from './features/product/services/forms/book-form-factory.service';
import { NewspaperFormFactory } from './features/product/services/forms/newspaper-form-factory.service';
import { NewspaperDetailBuilder } from './features/product/services/detail-builders/newspaper-detail-builder.service';
import { PRODUCT_DETAIL_BUILDERS } from './features/product/services/detail-builders/product-detail-builder.token';
import { DvdDetailBuilder } from './features/product/services/detail-builders/dvd-detail-builder.service';
import { CdDetailBuilder } from './features/product/services/detail-builders/cd-detail-builder.service';
import { BookDetailBuilder } from './features/product/services/detail-builders/book-detail-builder.service';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor])),

    {
      provide: PRODUCT_FORM_FACTORIES,
      useClass: DvdFormFactory,
      multi: true
    },
    {
      provide: PRODUCT_FORM_FACTORIES,
      useClass: CdFormFactory,
      multi: true
    },
    {
      provide: PRODUCT_FORM_FACTORIES,
      useClass: BookFormFactory,
      multi: true
    },
    {
      provide: PRODUCT_FORM_FACTORIES,
      useClass: NewspaperFormFactory,
      multi: true
    },
    {
      provide: PRODUCT_DETAIL_BUILDERS,
      useClass: BookDetailBuilder,
      multi: true
    },
    {
      provide: PRODUCT_DETAIL_BUILDERS,
      useClass: CdDetailBuilder,
      multi: true
    },
    {
      provide: PRODUCT_DETAIL_BUILDERS,
      useClass: DvdDetailBuilder,
      multi: true
    },
    {
      provide: PRODUCT_DETAIL_BUILDERS,
      useClass: NewspaperDetailBuilder,
      multi: true
    },
  ]
};