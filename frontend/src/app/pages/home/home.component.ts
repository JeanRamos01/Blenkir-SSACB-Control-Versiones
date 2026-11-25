import { AfterViewInit, Component, ElementRef, OnDestroy } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent implements AfterViewInit, OnDestroy {
  private observer?: IntersectionObserver;
  private readonly onScroll = () => {
    const navbar = this.host.nativeElement.querySelector('.navbar-blenkir') as HTMLElement | null;
    navbar?.classList.toggle('navbar-scrolled', window.scrollY > 50);
  };

  constructor(private readonly host: ElementRef<HTMLElement>) {}

  ngAfterViewInit(): void {
    const root = this.host.nativeElement;
    const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    const revealTargets = root.querySelectorAll<HTMLElement>(
      '.hero-content .col-lg-7, .hero-card, .stat-item, .section-image, #nosotros .col-lg-6, ' +
      '.feature-card, .level-card, .news-card, .cta-box, .contact-card, .footer .row > div'
    );

    revealTargets.forEach((element, index) => {
      element.classList.add('motion-reveal');
      element.style.setProperty('--reveal-delay', `${(index % 4) * 85}ms`);
    });

    if (!reduceMotion && 'IntersectionObserver' in window) {
      this.observer = new IntersectionObserver((entries, observer) => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            entry.target.classList.add('is-visible');
            observer.unobserve(entry.target);
          }
        });
      }, { threshold: 0.12, rootMargin: '0px 0px -35px 0px' });
      revealTargets.forEach(element => this.observer?.observe(element));
    } else {
      revealTargets.forEach(element => element.classList.add('is-visible'));
    }

    const canHover = window.matchMedia('(hover: hover) and (pointer: fine)').matches;
    if (!reduceMotion && canHover) {
      root.querySelectorAll<HTMLElement>('.hero-card, .feature-card, .level-card, .news-card, .contact-card')
        .forEach(card => {
          card.classList.add('tilt-card');
          card.addEventListener('pointermove', this.handlePointerMove);
          card.addEventListener('pointerleave', this.handlePointerLeave);
        });
    }

    root.querySelectorAll<HTMLAnchorElement>('.navbar-blenkir .nav-link:not(.dropdown-toggle)')
      .forEach(link => link.addEventListener('click', this.closeMobileMenu));

    this.onScroll();
    window.addEventListener('scroll', this.onScroll, { passive: true });
  }

  private readonly handlePointerMove = (event: PointerEvent): void => {
    const card = event.currentTarget as HTMLElement;
    const rect = card.getBoundingClientRect();
    const x = (event.clientX - rect.left) / rect.width;
    const y = (event.clientY - rect.top) / rect.height;
    card.style.setProperty('--rotate-y', `${(x - 0.5) * 7}deg`);
    card.style.setProperty('--rotate-x', `${(0.5 - y) * 7}deg`);
  };

  private readonly handlePointerLeave = (event: PointerEvent): void => {
    const card = event.currentTarget as HTMLElement;
    card.style.setProperty('--rotate-x', '0deg');
    card.style.setProperty('--rotate-y', '0deg');
  };

  private readonly closeMobileMenu = (): void => {
    const menu = this.host.nativeElement.querySelector('#menuPrincipal') as HTMLElement | null;
    const bootstrap = (window as any).bootstrap;
    if (menu?.classList.contains('show') && bootstrap?.Collapse) {
      bootstrap.Collapse.getOrCreateInstance(menu).hide();
    }
  };

  ngOnDestroy(): void {
    this.observer?.disconnect();
    window.removeEventListener('scroll', this.onScroll);
    const root = this.host.nativeElement;
    root.querySelectorAll<HTMLElement>('.tilt-card').forEach(card => {
      card.removeEventListener('pointermove', this.handlePointerMove);
      card.removeEventListener('pointerleave', this.handlePointerLeave);
    });
    root.querySelectorAll<HTMLAnchorElement>('.navbar-blenkir .nav-link:not(.dropdown-toggle)')
      .forEach(link => link.removeEventListener('click', this.closeMobileMenu));
  }
}
