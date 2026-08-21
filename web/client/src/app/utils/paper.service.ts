/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { PaperScope, Project, Size } from 'paper';
import { debounce } from './debounce';


@Injectable()
export class Paper {
  scope = new PaperScope();
  projects: InstanceType<typeof Project>[] = [];
  private _projectsById: object = {};

  constructor() {
    this.scope.activate();
  }

  addCanvas(canvas: HTMLCanvasElement, uuid?: string, isVertical?: BehaviorSubject<boolean>, resize: boolean = false) {
    let p: InstanceType<typeof Project> = this._projectsById[uuid] || null;

    if (p == null) {
      canvas.setAttribute('hidpi', 'off');
      p = new Project(canvas);
      this.projects.push(p);
      if (!!uuid) {
        this._projectsById[uuid] = p;
      }
    }

    p.activate();

    if (isVertical) {
      isVertical.subscribe(vert => {
        debounce(() => {
          this.resize(p);
        }, 125)();
      });
    }

    if (resize) {
      // resize the canvas window when the browser window is resized
      window.addEventListener('resize', debounce(() => {
        this.resize(p);
      }, 125), false);
    }

    return p;
  }

  resize(p: InstanceType<typeof Project>) {
    const bounds = (<HTMLElement>p.view.element.parentNode).getBoundingClientRect();

    if (bounds.width == 0 || bounds.height == 0) {
      for (const project of this.projects) {
        const projBounds = project.view.bounds;

        if (projBounds.height > 0 && projBounds.width > 0) {
          p.view.viewSize = new Size(projBounds.width, p.view.viewSize.height);
          return;
        }
      }
    }

    p.view.viewSize = new Size(bounds.width, p.view.viewSize.height);
  }
}
