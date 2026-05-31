import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-pagination',
  imports: [CommonModule],
  templateUrl: './pagination.component.html',
  styleUrl: './pagination.component.scss',
})
export class PaginationComponent {
  @Input() currentPage = 0;
  @Input() totalPages  = 0;
  @Output() onPage     = new EventEmitter<number>();

  get pages(): number[] {
    // Hiển thị tối đa 5 trang xung quanh trang hiện tại
    const range = 2;
    const start = Math.max(0, this.currentPage - range);
    const end   = Math.min(this.totalPages - 1, this.currentPage + range);
    return Array.from({ length: end - start + 1 }, (_, i) => start + i);
  }
}
