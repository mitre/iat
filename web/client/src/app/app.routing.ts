/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { Injectable, NgModule } from '@angular/core';
import { CanDeactivate, RouterModule, Routes, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';

import { HomeComponent } from './home/home.component';
import { CreateComponent } from './create/create.component';
import { CreateResolver } from './create/create.resolver';
import { ReviewComponent } from './review/review.component';
import { ReviewResolver } from './review/review.resolver';
import { UserComponent } from './user/user.component';
import { Observable } from 'rxjs';
import { ManageComponent } from './manage/manage.component';
import { HistoryComponent } from './history/history.component';
import { AdminComponent } from './admin/admin.component';
import { ReviewExportComponent } from './review-export/review-export.component';
import { PageNotFoundComponent } from './page-not-found/page-not-found.component';


// based on https://angular.io/guide/router#candeactivate-handling-unsaved-changes
export interface CanComponentDeactivate {
  canDeactivate: (url: string) => Observable<boolean> | Promise<boolean> | boolean;
}

@Injectable()
export class CanDeactivateGuard implements CanDeactivate<CanComponentDeactivate> {
  canDeactivate(component: CanComponentDeactivate,
                currentRoute: ActivatedRouteSnapshot,
                currentState: RouterStateSnapshot,
                nextState: RouterStateSnapshot) {
    return component.canDeactivate ? component.canDeactivate(nextState.url) : true;
  }
}

const appRoutes: Routes = [
  {
    path: '',
    redirectTo: 'home',
    pathMatch: 'full'
  },
  {
    path: 'review/:id',
    component: ReviewComponent,
    resolve: {
      config: ReviewResolver
    },
    canDeactivate: [CanDeactivateGuard]
  },
  {
    path: 'review',
    component: ReviewComponent,
    resolve: {
      config: ReviewResolver
    },
    canDeactivate: [CanDeactivateGuard]
  },
  {
    path: 'export',
    component: ReviewExportComponent,
    canDeactivate: [CanDeactivateGuard]
  },
  {
    path: 'create/:id',
    component: CreateComponent,
    resolve: {
      config: CreateResolver
    }
  },
  {
    path: 'create',
    component: CreateComponent,
    resolve: {
      config: CreateResolver
    },
    canDeactivate: [CanDeactivateGuard]
  },
  {
    path: 'manage',
    component: ManageComponent,
    canDeactivate: [CanDeactivateGuard]
  },
  {
    path: 'user',
    component: UserComponent
  },
  {
    path: 'home',
    component: HomeComponent
  },
  {
    path: 'history',
    component: HistoryComponent
  },
  {
    path: 'admin',
    component: AdminComponent
  },
  // otherwise redirect to home
  {path: '**',
  pathMatch: 'full',
  component: PageNotFoundComponent}
];


@NgModule({
  imports: [RouterModule.forRoot(appRoutes, {})],
  exports: [RouterModule],
  providers: [ReviewResolver, CreateResolver, CanDeactivateGuard],
})
export class AppRouting {
}
