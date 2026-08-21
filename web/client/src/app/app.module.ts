/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { HttpClientModule } from '@angular/common/http';
import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { AppComponent } from './app.component';
import { AppRouting } from './app.routing';
import { RouterModule } from '@angular/router';
import { BrowserModule } from '@angular/platform-browser';
import { Paper } from './utils/paper.service';

import { CreateComponent } from './create/create.component';
import { HomeComponent } from './home/home.component';
import { EbtsCreateService } from './create/ebts-create.service';
import { ResponseReviewService } from './review/response-review.service';
import { ReviewComponent } from './review/review.component';
import { ReportDetailsComponent } from './review/report-details.component';
import { CandidateContainerComponent, ViewCandidateDirective } from './review/candidate-container.component';
import { UnrollCompareDialogComponent } from './review/unroll-compare/unroll-compare-dialog.component';
import { UserComponent } from './user/user.component';
import { ProfileService } from './profile/profile.service';

import { IrisDisplayModule } from './iris-display/iris-display.module';
import { UtilsModule } from './utils/utils.module';
import { DialogService } from './utils/dialog.serivce';
import { AciiService } from './acii/acii.service';
import { PersonService } from './person/person.service';
import { FindIrisService } from './find-iris/find-iris.service';
import { ManageComponent } from './manage/manage.component';
import { NgxPaginationModule } from 'ngx-pagination';
import { TabsModule } from 'ngx-bootstrap/tabs';
import { PersonFilterPipe } from './manage/person-filter.pipe'; // <-- import the module
import { ErrorsHandler } from './error/errors-handler';
import { ErrorHandler } from '@angular/core';
import { HistoryComponent } from './history/history.component';
import { HistoryService } from './history/history.service';
import { SaveDialogComponent } from './save-dialog/save-dialog.component';
import { TshepiiService } from './tshepii/tshepii.service';
import { AdminComponent } from './admin/admin.component';
import { AdminFilterPipe } from './admin/admin-filter.pipe';
import { AdminService } from './admin/admin.service';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';
import { ModalModule } from 'ngx-bootstrap/modal';
import { TooltipModule } from 'ngx-bootstrap/tooltip';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ToolManagerService } from './iris-display/tool-manager.service';
import { BiqtContactService } from './biqt-contact/biqt-contact.service';

// Angular Material
import { MatExpansionModule } from '@angular/material/expansion';
import { MatSelectModule } from '@angular/material/select';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { MatCardModule } from '@angular/material/card';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTableModule } from '@angular/material/table';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSliderModule } from '@angular/material/slider';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatDialogModule } from '@angular/material/dialog';
import { MatSortModule } from '@angular/material/sort';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatChipsModule } from '@angular/material/chips';
import { MatSnackBarModule, MAT_SNACK_BAR_DEFAULT_OPTIONS } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatBadgeModule } from '@angular/material/badge';

import { DropzoneModule } from 'ngx-dropzone-wrapper';
import { ReviewExportComponent } from './review-export/review-export.component';
import { ReviewExportService } from './review-export/review-export.service';
import { NotificationsComponent } from './notifications/notifications.component';
import { ServiceStatusComponent } from './service-status/service-status.component';
import { ServiceStatusService } from './service-status/service-status.service';
import { CreateContentService } from './create/create.service';
import { ReviewContentService } from './review/review.service';
import { LogonInfo } from './types/logonInfo';
import { DatePipe, HashLocationStrategy, LocationStrategy } from '@angular/common';
import { NgChartsModule } from 'ng2-charts';
import { MAT_FORM_FIELD_DEFAULT_OPTIONS } from '@angular/material/form-field';
import { PageNotFoundComponent } from './page-not-found/page-not-found.component';
import {MatLegacyListModule} from '@angular/material/legacy-list';
import { PupilDilationToolService } from './pupil-dilation-tool/pupil-dilation-tool.service';
import { DualPDMService } from './dual-pdm/dual-pdm.service';
import { PdmConfirmationDialogModule } from './pdm-confirmation-dialog/pdm-confirmation-dialog.module';


@NgModule({
    imports: [
        BrowserModule,
        RouterModule,
        FormsModule,
        HttpClientModule,
        ReactiveFormsModule,
        AppRouting,
        UtilsModule,
        IrisDisplayModule,
        TabsModule.forRoot(),
        NgxPaginationModule,
        BsDropdownModule.forRoot(),
        TooltipModule.forRoot(),
        ModalModule.forRoot(),
        BrowserAnimationsModule,
        MatExpansionModule,
        MatSelectModule,
        MatToolbarModule,
        MatButtonModule,
        MatMenuModule,
        MatCardModule,
        MatTabsModule,
        MatTableModule,
        MatCheckboxModule,
        MatInputModule,
        MatPaginatorModule,
        MatButtonToggleModule,
        MatIconModule,
        MatDividerModule,
        MatSliderModule,
        MatDatepickerModule,
        MatNativeDateModule,
        MatDialogModule,
        MatSortModule,
        MatTooltipModule,
        MatChipsModule,
        MatSnackBarModule,
        DropzoneModule,
        MatProgressSpinnerModule,
        MatBadgeModule,
        NgChartsModule,
        MatLegacyListModule,
        PdmConfirmationDialogModule
    ],
  declarations: [
    AppComponent,
    HomeComponent,
    UserComponent,
    ReviewComponent,
    CreateComponent,
    CandidateContainerComponent,
    ViewCandidateDirective,
    UnrollCompareDialogComponent,
    ReportDetailsComponent,
    ServiceStatusComponent,
    ManageComponent,
    PersonFilterPipe,
    HistoryComponent,
    SaveDialogComponent,
    AdminComponent,
    AdminFilterPipe,
    ReviewExportComponent,
    NotificationsComponent,
    ServiceStatusComponent,
    PageNotFoundComponent
  ],
  providers: [
    {provide: MAT_FORM_FIELD_DEFAULT_OPTIONS, useValue: {appearance: 'outline'}},
    {provide: ErrorHandler, useClass: ErrorsHandler},
    {provide: LocationStrategy, useClass: HashLocationStrategy},
    Paper,
    DialogService,
    ResponseReviewService,
    ProfileService,
    EbtsCreateService,
    BiqtContactService,
    AciiService,
    TshepiiService,
    DualPDMService,
    EbtsCreateService,
    HistoryService,
    FindIrisService,
    PersonService,
    AdminService,
    ReviewExportService,
    CreateContentService,
    ReviewContentService,
    ServiceStatusService,
    LogonInfo,
    DatePipe,
    ToolManagerService,
    PupilDilationToolService,
    {provide: MAT_SNACK_BAR_DEFAULT_OPTIONS, useValue: {duration: 3000}}
  ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  bootstrap: [AppComponent]
})

export class AppModule {
}
