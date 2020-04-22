import {Component, ElementRef, Injector, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {AbstractComponent} from '../../common/component/abstract.component';
import {
  CountByBookType,
  PermissionChecker,
  PublicType,
  Workspace
} from '../../domain/workspace/workspace';
import {WorkspaceService} from '../service/workspace.service';
import {UpdateWorkspaceComponent} from './management/update-workspace.component';
import {DeleteWorkspaceComponent} from './management/delete-workspace.component';
import {WorkspaceListComponent} from './management/workspace-list.component';
import {SharedMemberComponent} from './permission/shared-member.component';
import {DatasourceComponent} from './etc/data-source.component';
import {EventBroadcaster} from "../../common/event/event.broadcaster";
import {WorkbookService} from "../../workbook/service/workbook.service";
import {DashboardService} from "../../dashboard/service/dashboard.service";
import {ActivatedRoute} from "@angular/router";
import {PopupService} from "../../common/service/popup.service";
import {UserProfile} from "../../domain/user/user-profile";
import {Folder, Hirearchies} from '../../domain/workspace/folder';

const DEFAULT_CONTENT_FILTER_INDEX = 0;

@Component({
  selector: 'workspace-summary-component',
  templateUrl: './workspace.summary.component.html',
  styleUrls: ['./workspace.summary.component.css']
})

export class WorkspaceSummaryComponent extends AbstractComponent{

  @ViewChild(SharedMemberComponent)
  private sharedMemberComponent: SharedMemberComponent;

  @ViewChild(DatasourceComponent)
  private datasourceComponent: DatasourceComponent;

  public workspace: Workspace;
  public workspaceId: string;
  public workspaceName: string;
  public workspaceDescription: string;
  public owner: UserProfile;
  public countByBookType: CountByBookType;
  public countByMemberType: { user: number, group: number };
  public countOfDataSources: number;
  public isRoot = true;
  public permissionChecker: PermissionChecker;
  public isSetNotebookServer: boolean = false;
  public isInfoType: string = null;
  public isShareType: boolean = false;
  public contentFilterIndex: number = DEFAULT_CONTENT_FILTER_INDEX;
  public folder: Folder;
  public contentFilter: any[] = [
    {key: 'all', value: this.translateService.instant('msg.comm.ui.list.dropbox.all')},
    {key: 'workbook', value: this.translateService.instant('msg.comm.ui.list.dropbox.workbook')},
    {key: 'notebook', value: this.translateService.instant('msg.comm.ui.list.dropbox.notebook')},
    {key: 'workbench', value: this.translateService.instant('msg.comm.ui.list.dropbox.workbench')},
  ];

  constructor(private broadCaster: EventBroadcaster,
              private workspaceService: WorkspaceService,
              private workbookService: WorkbookService,
              private dashboardService: DashboardService,
              private activatedRoute: ActivatedRoute,
              private popupService: PopupService,
              protected elementRef: ElementRef,
              protected injector: Injector) {
    super(elementRef, injector);
  }



  private _setWorkspaceData(loadNbServer: boolean, workspace?: Workspace,) {
    if (workspace) {
      if (workspace.active) {
        // 워크스페이스 데이터
        this.workspace = workspace;
        // 워크스페이스 이름
        this.workspaceName = workspace.name;
        // 워크스페이스 설명
        this.workspaceDescription = workspace.description;
        // 워크스페이스 소유자
        this.owner = workspace.owner;
        // 워크스페이스 아이디
        this.workspaceId = workspace.id;
        // 컨텐츠 수
        this.countByBookType = workspace.countByBookType;
        // 멤버 수
        this.countByMemberType = workspace.countByMemberType;
        // 데이터소스 수
        this.countOfDataSources = workspace.countOfDataSources;
        // 퍼미션
        (this.isRoot) && (this.permissionChecker = new PermissionChecker(workspace));

        if (loadNbServer) {
          this.workspaceService.getNotebookServers(this.workspaceId).then((result) => {
            this.isSetNotebookServer = (result['_embedded'] && 0 < result['_embedded'].connectors.length);
          });
        }

        this.contentFilter.forEach(filter => {
          filter.key === 'all'
            ? filter.value = this.translateService.instant('msg.comm.ui.list.dropbox.all', { value: this.workspace.books.filter(book => book.type !== 'folder').length })
            : filter.value = this.translateService.instant('msg.comm.ui.list.dropbox.' + filter.key, { value: this.workspace.books.filter(book => (book.type !== 'folder') && book.type === filter.key).length })
        });

      } else {
        // 워크스페이스 이름
        this.workspaceName = workspace.name;
        // 워크스페이스 설명
        this.workspaceDescription = workspace.description;
        // 경고창 표시
        this.openAccessDeniedConfirm();
      }
    }
  } // function - _setWorkspaceData



  public datasourceView() {
    if (this.isInfoType === 'member' || this.isInfoType === null) {
      this.sharedMemberComponent.close(false);
      this.isInfoType = 'datasource';
      this.datasourceComponent.init(this.workspaceId);
    } else {
      this.datasourceComponent.close();
    }
  }

  public sharedMemberView() {
    if (this.isInfoType === 'datasource' || this.isInfoType === null) {
      this.datasourceComponent.close();
      this.isInfoType = 'member';
      this.sharedMemberComponent.init(this.workspace);
    } else {
      this.sharedMemberComponent.close(false);
    }
  }



}

