# Design System Specification: The Serene Scholar

## 1. Overview & Creative North Star
**Creative North Star: "The Tactile Papercraft Atelier"**

This design system moves away from the over-stimulated, neon-saturated "gamification" typical of children's apps. Instead, we embrace a high-end editorial approach tailored for young learners. By combining the precision of a Swiss grid with the softness of a boutique Montessori environment, we create a "Soft & Structured Explorer" experience.

The system breaks the "template" look through **intentional density**. We do not fear content; we organize it through sophisticated tonal layering rather than clunky dividers. The result is a space-efficient interface that feels like a premium physical workbook—calm, focused, and intellectually inviting.

---

## 2. Colors & Surface Philosophy
The palette is rooted in nature—Sage (`primary`), Slate Blue (`secondary`), and Warm Almond (`surface`). We avoid harsh blacks to prevent ocular fatigue, opting instead for `on-surface` (#31332E) for deep, readable contrast.

### The "No-Line" Rule
To maintain a high-end feel, **1px solid borders are strictly prohibited for sectioning.** Structural boundaries must be defined solely through:
- **Tonal Transitions:** Transitioning from `surface` to `surface-container-low`.
- **Vertical Rhythm:** Using the Spacing Scale (e.g., `8` or `10`) to create "islands" of content.

### Surface Hierarchy & Nesting
Treat the UI as a series of nested paper sheets.
- **Base Layer:** `surface` (#FBF9F4) for the main application background.
- **Section Layer:** `surface-container-low` (#F5F4ED) for grouping related learning modules.
- **Interactive Layer:** `surface-container-lowest` (#FFFFFF) for cards or input areas to make them "pop" with a clean, crisp lift.

### Signature Textures
Apply a subtle linear gradient to Primary CTAs (transitioning from `primary` to `primary-dim`) to give buttons a "pressed silk" feel. Use Glassmorphism (Backdrop Blur: 12px, Opacity: 80%) for floating navigation bars using the `surface-variant` token to ensure the background "breathes" through the UI.

---

## 3. Typography
We utilize **Lexend** exclusively. Its hyper-legible, rounded glyphs are scientifically designed to reduce visual stress, making it the perfect choice for children learning complex Mandarin characters alongside Latin scripts.

* **Display (lg/md):** Used for celebratory moments or lesson titles. Letter spacing should be set to `-0.02em` to maintain a tight, editorial feel.
* **Headline (sm):** The primary engine for module headers. Use `on-surface-variant` to keep the hierarchy sophisticated.
* **Title (md/sm):** Used for Mandarin character labels. Pair `title-md` for the Hanzi and `label-md` for the Pinyin to create a clear vertical lockup.
* **Body (md):** The workhorse for instructions. Ensure a line height of `1.5` to assist early readers.

---

## 4. Elevation & Depth
In this system, depth is organic, not synthetic.

* **The Layering Principle:** Avoid shadows for static elements. A `surface-container-highest` card placed on a `surface` background provides enough contrast for the eye to perceive a layer change.
* **Ambient Shadows:** For "floating" elements like modals or the Panda mascot, use a "Cloud Shadow": `box-shadow: 0 12px 32px rgba(49, 51, 46, 0.06)`. This uses a tinted version of our `on-surface` color, mimicking natural light.
* **The Ghost Border:** If a module requires a container (e.g., a quiz card), use the `outline-variant` token at **15% opacity**. It provides a "whisper" of a boundary without cluttering the grid.

---

## 5. Components

### Interactive Modules (Primary)
* **Shape:** Rounded Squares (`rounded-lg` / 1rem).
* **Styling:** Use `secondary-container` backgrounds. This maximizes "tile density," allowing 4-6 learning modules to fit on a single screen without feeling cramped.
* **State:** On press, shift background to `secondary-fixed-dim`.

### Action Buttons (Secondary)
* **Shape:** Pill (`rounded-full`).
* **Styling:** `primary` background with `on-primary` text. These are reserved for "Next," "Submit," or "Play."
* **Padding:** Use Spacing `3` (vertical) and `6` (horizontal).

### The Supportive Panda (Mascot Integration)
The mascot is an **architectural element**, not a focal point.
* **Placement:** Crop the Panda so only the ears and eyes peek from the `surface-container` edges.
* **Interaction:** Use the mascot to point toward the "Next" button or to highlight a Mandarin character using a `tertiary-container` speech bubble.

### Progress Indicators (Space Efficient)
Forbid bulky progress bars. Use a "Thread Indicator": a 2px horizontal line using `primary-fixed-dim` that runs across the very top of the viewport, staying out of the way of the content grid.

### Learning Cards & Lists
* **Rule:** No dividers.
* **Separation:** Use a `2.5` (0.5rem) margin between list items. Use a subtle tonal shift (`surface-container-low`) on every second item to create a "zebra-stripe" rhythm for better eye-tracking.

---

## 6. Do's and Don'ts

### Do:
* **Do** use `surface-bright` for areas where a child needs to focus intensely (like drawing characters).
* **Do** embrace asymmetry. Placing the Panda mascot slightly off-center creates a more custom, premium feel.
* **Do** use the Spacing Scale religiously to maintain "Dense but Breathable" layouts.

### Don't:
* **Don't** use pure black (#000000) or pure white (#FFFFFF) for text. It causes "halo" effects for young eyes.
* **Don't** use heavy "drop shadows." They look dated and consume visual "weight" that belongs to the content.
* **Don't** use "Bounce" animations. Stick to subtle "Fade-In" or "Soft Slide" (300ms) to maintain the serene atmosphere.
