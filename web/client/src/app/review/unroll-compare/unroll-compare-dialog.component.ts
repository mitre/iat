/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

import { Component, OnInit, OnDestroy, AfterContentInit, Inject } from '@angular/core';
import { Paper } from '../../utils/paper.service';
import { Project, Raster, Path, Group, Point, Rectangle, Layer } from 'paper';
import { Annotation, AnnotationType } from '../../types/annotation';
import { ClosestPair } from '../../tshepii/closest-pair';
import Color = paper.Color;
import { MatDialogRef } from '@angular/material/dialog';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { LinkedMap } from '../../types/linked-map';

interface UnrollDisplay {
  width: number;
  height: number;
  project: InstanceType<typeof Project>;
  raster: InstanceType<typeof Raster>;
  loading: boolean;
  offset: number;
  canvas: HTMLCanvasElement;
}

declare let jQuery: any;

const DEFAULT_UNROLL_DISPLAY = {
  width: 0,
  height: 0,
  project: null,
  raster: null,
  loading: true,
  canvas: null,
  offset: 0
};

@Component({
  selector: 'app-unroll-compare-dialog',
  templateUrl: './unroll-compare-dialog.component.html',
  styleUrls: ['./unroll-compare-dialog.component.css'],

})
export class UnrollCompareDialogComponent implements OnInit, OnDestroy, AfterContentInit {
  public probe: UnrollDisplay = Object.assign({}, DEFAULT_UNROLL_DISPLAY);
  public candidate: UnrollDisplay = Object.assign({}, DEFAULT_UNROLL_DISPLAY);
  public overlay: UnrollDisplay = Object.assign({}, DEFAULT_UNROLL_DISPLAY);

  private probeAnnotations = [];
  private candidateAnnotations = [];

  private probePointAnnotations = [];
  private candidatePointAnnotations = [];

  private probeLayer: InstanceType<typeof Layer>;
  private candidateLayer: InstanceType<typeof Layer>;

  previousOverlayValue = 1;
  _showClosePoints = 10;

  private inView = false;
  showOverlayOptions = false;
  private probeHeightOffset = 0;
  private candidateHeightOffset = 0;

  private divAdjustX = 40;
  private divAdjustY = 40;

  _showProbeAnnotations = true;
  _showCandidateAnnotations = true;

  slider = {
    value: 0.5
  };

  constructor(public dialogRef: MatDialogRef<UnrollCompareDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public _input: any,
              private paperSvc: Paper) {
  }

  ngOnInit() {
    // Big change to calculate points at beginning, and reference those points later.
    // Old way recalculated every reset
    this.walkLoadPoints(this.probePointAnnotations, this._input.probe.toolManager.annotations);
    this.walkLoadPoints(this.candidatePointAnnotations, this._input.candidate.toolManager.annotations);
    this.resetCanvas();
  }

  walkLoadPoints(pointAnnotations: any[], annotations: LinkedMap<Annotation>) {
    for (const annotation of annotations) {
      if (annotation.type == AnnotationType.POINT) {
        pointAnnotations.push(annotation);
      } else if (annotation.type == AnnotationType.GROUP) {
        this.walkLoadPoints(pointAnnotations, annotation.children);
      }
    }
  }

  resetCanvas() {
    const probe = this._input.probe.toolManager.unrolledRaster;
    const candidate = this._input.candidate.toolManager.unrolledRaster;

    this.probe.raster = probe.clone({insert: false});
    this.probe.width = probe.bounds.width + 30;
    this.probe.height = probe.bounds.height + 30;

    this.candidate.raster = candidate.clone({insert: false});
    this.candidate.width = candidate.bounds.width + 30;
    this.candidate.height = candidate.bounds.height + 30;

    this.overlay.width = Math.max(this.probe.width, this.candidate.width) + 10;
    this.overlay.height = Math.max(this.probe.height, this.candidate.height) + 10;
    this.overlay.width *= 2;
    this.overlay.height *= 2;

    for (const annotation of this.probePointAnnotations) {
      const newPt = this._input.probe.toolManager.mainToUnroll(new Point(annotation.value.points[0].x, annotation.value.points[0].y));

      if (newPt.x == -1 || newPt.y == -1) {
continue;
}

      //adjust for the 15 px boundary on the unroll compare modal
      newPt.x += 15 + this.probe.offset;
      newPt.y += 15 + this.probe.offset;

      const circle = new Path.Circle(newPt, annotation.value.data['radius']);
      circle.fillColor = annotation.value.data['fillColor'];
      circle.strokeWidth = annotation.value.data['strokeWidth'];
      circle.strokeColor = annotation.value.data['strokeColor'];
      this.probeAnnotations.push(circle);
    }

    this.probeLayer = new Layer({name: 'unroll-annotations'});
    const probeGroup = this.probeLayer.addChild(new Group());

    for (const path of this.probeAnnotations) {
      probeGroup.addChild(path);
    }

    for (const annotation of this.candidatePointAnnotations) {
      const newPt = this._input.candidate.toolManager.mainToUnroll(new Point(annotation.value.points[0].x, annotation.value.points[0].y));

      if (newPt.x == -1 || newPt.y == -1) {
continue;
}

      //adjust for the 15 px boundary on the unroll compare modal
      newPt.x += 15 + this.candidate.offset;
      newPt.y += 15 + this.candidate.offset;

      const circle = new Path.Circle(newPt, annotation.value.data['radius']);
      circle.fillColor = annotation.value.data['fillColor'];
      circle.strokeWidth = annotation.value.data['strokeWidth'];
      circle.strokeColor = annotation.value.data['strokeColor'];
      this.candidateAnnotations.push(circle);
    }

    this.candidateLayer = new Layer({name: 'unroll-annotations'});
    const candidateGroup = this.candidateLayer.addChild(new Group());

    for (const path of this.candidateAnnotations) {
      candidateGroup.addChild(path);
    }
  }

  ngOnDestroy() {
  }

  ngAfterContentInit() {
    this.inView = true;
    setTimeout(() => this.drawCompareItems([1.25, 0.75, 0.75], [0.75, 1.25, 0.75]), 500);
  }

  onSliderChange() {
    this.overlay.project.activeLayer.children['probe'].opacity = 1 - this.slider.value;
    this.overlay.project.activeLayer.children['candidate'].opacity = this.slider.value;
  }

  private static tintRaster(raster: InstanceType<typeof Raster>, channel) {
    for (let y = 0; y < raster.height; y++) {
      for (let x = 0; x < raster.width; x++) {
        const color = raster.getPixel(x, y);
        color.red *= channel[0];
        color.green *= channel[1];
        color.blue *= channel[2];
        raster.setPixel(x, y, color);
      }
    }
  };

  static drawSliderControls(ud: UnrollDisplay, drawFn: () => void) {
    const project: InstanceType<typeof Project> = ud.project;

    project.activate();

    const SLIDER_ICON_RADIUS = 8;
    const bounds = project.activeLayer.firstChild.bounds.clone();
    const ctrlsGroup = project.activeLayer.addChild(new Group());

    // top slider control
    const topPoint = new Point(project.view.center.x, bounds.y - 3);
    const botPoint = new Point(project.view.center.x, bounds.y + bounds.height + 5);

    const top = ctrlsGroup.addChild(new Path.RegularPolygon({
      center: topPoint.clone(),
      sides: 3,
      radius: SLIDER_ICON_RADIUS,
      fillColor: 'black'
    }));
    top.rotate(180);

    // bottom slider control
    const bottom = ctrlsGroup.addChild(new Path.RegularPolygon({
      center: botPoint.clone(),
      sides: 3,
      radius: SLIDER_ICON_RADIUS,
      fillColor: 'black'
    }));

    const centerLine = ctrlsGroup.addChild(new Path.Line({
      from: topPoint.clone(),
      to: botPoint.clone(),
      strokeWidth: 3,
      strokeColor: 'black',
      visible: false
    }));

    let isMouseDown = false;
    let isUpdating = false;
    ctrlsGroup.on({
      mouseenter: evt => {
        project.view.element.classList.add('draggable');
        centerLine.visible = true;
      },
      mouseleave: evt => {
        if (!isMouseDown) {
          project.view.element.classList.remove('draggable');
          project.view.element.classList.remove('dragging');
        }
        centerLine.visible = false;
      },
      mousedown: evt => {
        project.view.element.classList.remove('draggable');
        project.view.element.classList.add('dragging');
        isMouseDown = true;
      },
      mouseup: evt => {
        project.view.element.classList.add('draggable');
        project.view.element.classList.remove('dragging');
        isMouseDown = false;
        centerLine.visible = false;
      },
      mousedrag: evt => {
        // ignore events in which the x doesn't "move"
        if (isUpdating || evt.delta.x === 0) {
          return false;
        }

        const position = ctrlsGroup.position.x + evt.delta.x;

        centerLine.visible = true;

        if (position < 15 || position > 375) {
          return;
        }

        isUpdating = true;

        ctrlsGroup.position.x += evt.delta.x;

        const raster: InstanceType<typeof Raster> = ud.raster;
        let main: InstanceType<typeof Raster>;
        let sub: InstanceType<typeof Raster>;

        // left is negative, right is positive

        if (evt.delta.x < 0) {
          // shifting to the left

          // NOTE: This rect is from the perspective of the Raster (not the overall view/canvas)
          const mainRect = new Rectangle({
            point: new Point(Math.abs(evt.delta.x), 0),
            size: [raster.bounds.width + evt.delta.x, raster.bounds.height]
          });
          main = raster.getSubRaster(mainRect);

          // NOTE: This rect is from the perspective of the Raster (not the overall view/canvas)
          const subBounds = new Rectangle({
            point: new Point(0, 0),
            size: [Math.abs(evt.delta.x), raster.bounds.height]
          });
          sub = raster.getSubRaster(subBounds);

          main.translate(new Point(evt.delta.x, 0));
          sub.translate(new Point(main.bounds.width, 0));

          ud.offset += evt.delta.x;
        } else if (evt.delta.x > 0) {
          // shifting to the right

          // NOTE: This rect is from the perspective of the Raster (not the overall view/canvas)
          const mainRect = new Rectangle({
            point: new Point(0, 0),
            size: [raster.bounds.width - evt.delta.x, raster.bounds.height]
          });
          main = raster.getSubRaster(mainRect);

          // NOTE: This rect is from the perspective of the Raster (not the overall view/canvas)
          const subBounds = new Rectangle({
            point: new Point(raster.bounds.width - evt.delta.x, 0),
            size: [evt.delta.x, raster.bounds.height]
          });
          sub = raster.getSubRaster(subBounds);

          main.translate(new Point(evt.delta.x, 0));
          sub.translate(new Point(main.bounds.width * -1, 0));

          ud.offset += evt.delta.x;
        }
        // else - do nothing, no change in x value

        // Place both Rasters instance in a Group instance and rasterize the group to
        // continuously have only a single image/raster
        const g = new Group({
          insert: false,
          children: [main, sub]
        });
        const rast = (g.rasterize.bind(g, null, false))();   // defined this way because TypeScript is dumb
        raster.replaceWith(rast);
        ud.raster = rast;

        drawFn();

        isUpdating = false;

        //adjust the annotations
        for (const layer of ud.project.layers) {
          if (layer.name != undefined
            && layer.name == 'unroll-annotations') {

            for (const group of layer.children) {

              for (const point of group.children) {
                point.position.x += evt.delta.x;

                if (point.position.x <= 15) {
                  point.position.x = (ud.raster.bounds.width + 15) - (15 - point.position.x);
                } else if (point.position.x >= (ud.raster.bounds.width + 15)) {
                  point.position.x = point.position.x - ud.raster.bounds.width;
                }
              }//end of for points
            }//end fo for group
          }//end of if unroll layer
        }//end of for layers
      }
    });
  }

  drawUnrollCanvas(ud: UnrollDisplay, colorChannel: number[]) {
    ud.project = this.paperSvc.addCanvas(ud.canvas);
    ud.project.activeLayer.addChild(ud.raster);

    UnrollCompareDialogComponent.tintRaster(ud.raster, colorChannel);
    ud.raster.position = ud.project.view.center;
  }

  probeInit(canvasRef) {
    this.probe.canvas = canvasRef.nativeElement;
  }

  candidateInit(canvasRef) {
    this.candidate.canvas = canvasRef.nativeElement;
  }

  drawOverlay() {
    const overlayFactor = 2.0;
    this.overlay.project.activeLayer.removeChildren();

    const probe = this.probe.raster.clone({insert: false});
    probe.name = 'probe';
    probe.opacity = 1 - this.slider.value;

    const candidate = this.candidate.raster.clone({insert: false});
    candidate.name = 'candidate';
    candidate.opacity = this.slider.value;

    this.overlay.project.activeLayer.addChildren([candidate, probe]);

    probe.scale(overlayFactor);
    candidate.scale(overlayFactor);

    if (this._showProbeAnnotations) {
      for (const annotation of this.probePointAnnotations) {
        const newPt = this._input.probe.toolManager.mainToUnroll(new Point(annotation.value.points[0].x, annotation.value.points[0].y));
        if (newPt.x == -1 || newPt.y == -1) {
continue;
}

        //adjust for the 15 px boundary on the unroll compare modal
        newPt.x *= overlayFactor;
        newPt.y *= overlayFactor;

        newPt.x += this.divAdjustX + (this.probe.offset * overlayFactor);
        newPt.y += this.divAdjustY + this.probeHeightOffset;

        if (newPt.x <= this.divAdjustX) {
          newPt.x = (probe.bounds.width + this.divAdjustX) - (this.divAdjustX - newPt.x);
        } else if (newPt.x >= (probe.bounds.width + this.divAdjustX)) {
          newPt.x = newPt.x - probe.bounds.width;
        }

        const circle = new Path.Circle(newPt, annotation.value.data['radius']);
        circle.fillColor = annotation.value.data['fillColor'];
        circle.strokeWidth = annotation.value.data['strokeWidth'];
        circle.strokeColor = annotation.value.data['strokeColor'];
        this.overlay.project.activeLayer.addChild(circle);
      }
    }

    if (this._showCandidateAnnotations) {
      for (const annotation of this.candidatePointAnnotations) {
        const newPt = this._input.candidate.toolManager.mainToUnroll(new Point(annotation.value.points[0].x, annotation.value.points[0].y));

        if (newPt.x == -1 || newPt.y == -1) {
continue;
}

        //adjust for the 15 px boundary on the unroll compare modal
        newPt.x *= overlayFactor;
        newPt.y *= overlayFactor;

        newPt.x += this.divAdjustX + (this.candidate.offset * overlayFactor);
        newPt.y += this.divAdjustY + this.candidateHeightOffset;

        if (newPt.x <= this.divAdjustX) {
          newPt.x = (candidate.bounds.width + this.divAdjustX) - (this.divAdjustX - newPt.x);
        } else if (newPt.x >= (candidate.bounds.width + this.divAdjustX)) {
          newPt.x = newPt.x - candidate.bounds.width;
        }

        const circle = new Path.Circle(newPt, annotation.value.data['radius']);
        circle.fillColor = annotation.value.data['fillColor'];
        circle.strokeWidth = annotation.value.data['strokeWidth'];
        circle.strokeColor = annotation.value.data['strokeColor'];
        this.overlay.project.activeLayer.addChild(circle);
      }
    }

    if (this._showProbeAnnotations && this._showCandidateAnnotations && this._showClosePoints > 0) {
      const annotationsUsed = [];

      for (const pAnn of this.probePointAnnotations) {
        const currentPair = new ClosestPair();
        currentPair.distance = Number.MAX_SAFE_INTEGER;

        if (annotationsUsed.indexOf(pAnn) < 0) {
          for (const cAnn of this.candidatePointAnnotations) {
            if (annotationsUsed.indexOf(cAnn) < 0) {
              const pPoint = this._input.probe.toolManager.mainToUnroll(new Point(pAnn.value.points[0].x, pAnn.value.points[0].y));
              const cPoint = this._input.candidate.toolManager.mainToUnroll(new Point(cAnn.value.points[0].x, cAnn.value.points[0].y));

              if (pPoint.x == -1 || pPoint.y == -1) {
continue;
}

              //adjust for the 15 px boundary on the unroll compare modal
              pPoint.x *= overlayFactor;
              pPoint.y *= overlayFactor;

              pPoint.x += this.divAdjustX + (this.probe.offset * overlayFactor);
              pPoint.y += this.divAdjustY + this.probeHeightOffset;

              if (pPoint.x <= this.divAdjustX) {
                pPoint.x = (probe.bounds.width + this.divAdjustX) - (this.divAdjustX - pPoint.x);
              } else if (pPoint.x >= (probe.bounds.width + this.divAdjustX)) {
                pPoint.x = pPoint.x - probe.bounds.width;
              }

              if (cPoint.x == -1 || cPoint.y == -1) {
continue;
}

              //adjust for the 15 px boundary on the unroll compare modal
              cPoint.x *= overlayFactor;
              cPoint.y *= overlayFactor;

              cPoint.x += this.divAdjustX + (this.candidate.offset * overlayFactor);
              cPoint.y += this.divAdjustY + this.candidateHeightOffset;

              if (cPoint.x <= this.divAdjustX) {
                cPoint.x = (candidate.bounds.width + this.divAdjustX) - (this.divAdjustX - cPoint.x);
              } else if (cPoint.x >= (candidate.bounds.width + this.divAdjustX)) {
                cPoint.x = cPoint.x - candidate.bounds.width;
              }

              const tempDist = pPoint.getDistance(cPoint);
              if (tempDist <= this._showClosePoints && tempDist < currentPair.distance) {
                currentPair.point1 = pPoint;
                currentPair.point2 = cPoint;
                currentPair.anno1 = pAnn;
                currentPair.anno2 = cAnn;
                currentPair.distance = tempDist;
              }
            }
          }
        }

        if (currentPair.distance < this._showClosePoints) {
          annotationsUsed.push(currentPair.anno1);
          annotationsUsed.push(currentPair.anno2);

          const path = new Path();
          path.strokeColor = new Color('#FFFF00');
          path.strokeWidth = 2;
          path.add(currentPair.point1);
          path.add(currentPair.point2);

          this.overlay.project.activeLayer.addChild(path);
        }
      }
    }

    const probePosition = new Point(this.overlay.project.view.center.x,
      this.overlay.project.view.center.y + this.probeHeightOffset);
    const candidatePosition = new Point(this.overlay.project.view.center.x,
      this.overlay.project.view.center.y + this.candidateHeightOffset);
    probe.position = probePosition;
    candidate.position = candidatePosition;
  }

  overlayInit(canvasRef) {
    this.overlay.project = this.paperSvc.addCanvas(canvasRef.nativeElement);

    this.drawOverlay();
  }

  updateOverlay(overlayUpdate) {
    if (this.previousOverlayValue != overlayUpdate) {
      this.resetCanvas();

      jQuery('#probeAnnotationsLabel').removeClass('btn-primary');
      jQuery('#probeAnnotationsLabel').removeClass('btn-primary');
      jQuery('#probeAnnotationsLabel').removeClass('btn-danger');

      jQuery('#candidateAnnotationsLabel').removeClass('btn-success');
      jQuery('#candidateAnnotationsLabel').removeClass('btn-warning');
      jQuery('#candidateAnnotationsLabel').removeClass('btn-primary');

      if (overlayUpdate == 1) {
        this.drawCompareItems([1.25, 0.75, 0.75], [0.75, 1.25, 0.75]);

        jQuery('#probeAnnotationsLabel').addClass('btn-danger');
        jQuery('#candidateAnnotationsLabel').addClass('btn-success');

      } else if (overlayUpdate == 2) {
        this.drawCompareItems([.75, 0.75, 1.25], [1.25, 1.25, 0.75]);

        jQuery('#probeAnnotationsLabel').addClass('btn-primary');
        jQuery('#candidateAnnotationsLabel').addClass('btn-warning');
      } else {
        this.drawCompareItems([1, 1, 1], [1, 1, 1]);

        jQuery('#probeAnnotationsLabel').addClass('btn-primary');
        jQuery('#candidateAnnotationsLabel').addClass('btn-primary');
      }

      this.drawOverlay();
      this.previousOverlayValue = overlayUpdate;
    }
  }

  drawCompareItems(probeColorChannel, candidateColorChannel) {
    this.drawUnrollCanvas(this.probe, probeColorChannel);
    this.drawUnrollCanvas(this.candidate, candidateColorChannel);

    this.probe.project.addLayer(this.probeLayer);
    this.candidate.project.addLayer(this.candidateLayer);

    UnrollCompareDialogComponent.drawSliderControls(this.probe, this.drawOverlay.bind(this));
    UnrollCompareDialogComponent.drawSliderControls(this.candidate, this.drawOverlay.bind(this));

    this.probe.loading = false;
    this.candidate.loading = false;
  }

  showProbeAnnotations() {
    this._showProbeAnnotations = !this._showProbeAnnotations;
    this.drawOverlay();
  }

  showClosePoints() {
    this.drawOverlay();
  }

  showCandidateAnnotations() {
    this._showCandidateAnnotations = !this._showCandidateAnnotations;
    this.drawOverlay();
  }

  moveProbeUp() {
    if (this.probeHeightOffset > -10) {
      this.probeHeightOffset -= 1;
    }
    this.drawOverlay();
  }

  moveProbeDown() {
    if (this.probeHeightOffset < 10) {
      this.probeHeightOffset += 1;
    }
    this.drawOverlay();
  }

  moveCandidateUp() {
    if (this.candidateHeightOffset > -20) {
      this.candidateHeightOffset -= 1;
    }
    this.drawOverlay();
  }

  moveCandidateDown() {
    if (this.candidateHeightOffset < 20) {
      this.candidateHeightOffset += 1;
    }
    this.drawOverlay();
  }

  resetHeights() {
    this.candidateHeightOffset = 0;
    this.probeHeightOffset = 0;
    this.drawOverlay();
  }

  closeDialog() {
    this.dialogRef.close();
  }

}
