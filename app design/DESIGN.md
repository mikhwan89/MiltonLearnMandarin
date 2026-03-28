# Design System Specification: The Playful Voyager

## 1. Overview & Creative North Star
This design system is built to transform Mandarin learning into a tactile, joyful exploration for children. The **Creative North Star** is **"The Paper-Craft Playground."** 

Moving away from the rigid, dark, and flat modularity seen in current educational apps, this system treats the UI as a series of physical, soft-edged layers. We break the "template" feel by utilizing exaggerated corner radii, intentional asymmetry, and a depth model that mimics stacked organic materials rather than digital pixels. The goal is to create an environment that feels less like a "tool" and more like a premium, interactive storybook.

## 2. Colors
Our palette moves away from high-contrast black backgrounds to a warm, "Paper White" base, utilizing sun-drenched yellows and nature-inspired tones to maintain high energy without causing visual fatigue.

### Color Principles
- **The "No-Line" Rule:** 1px solid borders are strictly prohibited for defining sections. Structure must be achieved through background shifts. For instance, a `surface-container-low` card should sit on a `surface` background to create a boundary through tone alone.
- **The Glass & Gradient Rule:** To ensure CTAs feel "clickable" and premium, use subtle linear gradients (e.g., `primary` to `primary-container`). Floating navigation or temporary overlays should utilize **Glassmorphism** (semi-transparent surface colors with a 20px backdrop blur) to maintain a sense of place.
- **Nesting Hierarchy:** Use the `surface-container` tiers (Lowest to Highest) to define importance. A learning activity might sit on `surface-container-low`, while the interactive "Word Tiles" sit on `surface-container-lowest` to pop forward.

### Key Tokens
- **Background/Base:** `#f8f7f0` (Surface) - A warm, creamy off-white.
- **Action (Primary):** `#705900` / `#fecb00` (Sunny Yellow) - Used for primary progress and joy.
- **Calm (Secondary):** `#006384` / `#97daff` (Soft Blue) - Used for listening exercises and steady states.
- **Growth (Tertiary):** `#246830` / `#b4fdb4` (Friendly Green) - Used for success states and "correct" feedback.

## 3. Typography
The typography is designed to be "Whimsical yet Legible." We use a high-contrast scale where display text is large and expressive, while functional text remains extremely clear for early readers.

- **Display & Headlines (Plus Jakarta Sans):** This typeface provides the "Signature" look. It’s used in `display-lg` (3.5rem) and `headline-md` (1.75rem) to celebrate milestones and introduce new characters. Its geometric yet soft nature feels modern and editorial.
- **Body & Titles (Lexend):** Lexend was specifically designed to improve reading fluency. We use it for all instructional content. Its rounded terminals mirror our UI’s `24px+` corners, creating a cohesive visual language.
- **Visual Soul:** By pairing the bold, slightly idiosyncratic weights of Plus Jakarta Sans with the clean, rhythmic spacing of Lexend, we guide the child's eye from "The Big Idea" (Headline) to "The Small Action" (Body).

## 4. Elevation & Depth
We reject traditional drop shadows in favor of **Tonal Layering**.

- **The Layering Principle:** Depth is created by "stacking." Place a `surface-container-lowest` (pure white) card on a `surface-container-low` background. This creates a soft, natural lift that feels like a physical card resting on a table.
- **Ambient Shadows:** For elements that truly "float" (like a congratulatory pop-up), use a shadow tinted with the `on-surface` color (`#2e2f2b`) at 5% opacity with a blur of `32px`. Avoid grey/black shadows; they muddy the vibrant palette.
- **Ghost Borders:** If an element lacks contrast against its background, use a 2px "Ghost Border" using the `outline-variant` token at **15% opacity**. This provides a hint of a container without the harshness of a solid stroke.
- **Glassmorphism:** Use semi-transparent layers for top navigation bars. This allows the "vibrant greens and yellows" of the scrollable content to peek through, making the app feel deep and integrated.

## 5. Components

### Interactive Elements
- **Word Tiles (Buttons):** Use `xl` (3rem) or `full` rounded corners. Tiles should have a subtle gradient and a 4px bottom-weighted "press" state (achieved via a darker tonal shift) to simulate a physical button.
- **Chips:** Selection chips should use the `surface-container-highest` token when unselected and flip to `primary-container` when active.
- **Input Fields:** Large, pill-shaped (`full` roundedness) containers. Forbid standard rectangular boxes. Use `surface-container-low` as the base fill.

### Content Containers
- **Lesson Cards:** Use `lg` (2rem) rounded corners. Forbid dividers. Separate content using `spacing-6` (2rem) and background color shifts.
- **Progress Bars:** Use a thick, 16px height with `full` rounded caps. The track should be `surface-container-highest` and the fill a gradient from `primary` to `primary-fixed`.

### Specialized Components
- **The "Hero Character" Slot:** A dedicated container with an asymmetrical `xl` corner treatment (e.g., top-left 3rem, others 1rem) to house illustrative Mandarin characters or mascots.
- **Tone Triggers:** Large, circular touch targets for the four Mandarin tones, each utilizing a unique tonal shade (Secondary for blue, Tertiary for green) to provide a chromatic mnemonic for the child.

## 6. Do’s and Don’ts

### Do:
- **Do** use white space as a structural tool. Use `spacing-8` (2.75rem) to separate major activity groups.
- **Do** lean into asymmetry. Off-center illustrations feel more "playful" and less "corporate."
- **Do** ensure all touch targets are at least `spacing-12` (4rem) in height for small hands.
- **Do** use "Paper-on-Paper" layering for all card layouts.

### Don’t:
- **Don’t** use 1px solid borders or #000000 shadows.
- **Don’t** use "Dark Mode." This system is optimized for a light, energetic, and high-clarity environment.
- **Don’t** use sharp corners (under 16px). Every edge must feel safe and soft.
- **Don’t** use dividers. If you need to separate two items, increase the spacing or change the background tone of one item.