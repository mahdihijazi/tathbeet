# Competitive Scan and Gap Analysis for Tathbeet

Last updated: March 8, 2026

This document benchmarks `Tathbeet` against 5 currently available Quran memorization or revision products found on the App Store and Google Play. Ranking is based on closeness to the core `Tathbeet` idea in `PRD.md`: recurring Quran revision, manageable daily planning, reminders, progress tracking, and support for maintaining memorized content over time.

If a feature is not clearly mentioned in the store listing or official help page, it is treated as `not advertised`, not `definitely absent`.

## Ranked Competitors

### 1. Mudakir

Why it is the closest fit:

- positioned around maintaining memorization, not just learning new content
- explicitly offers structured daily revision plans
- includes completion tracking, consistency support, manual review logging, and optional reminders
- keeps the product focused and low-distraction, which is close to `Tathbeet`'s calm daily-flow direction

Best evidence found:

- “Generate structured daily revision plans”
- “Track completion and consistency”
- “Log manual reviews”
- “Stay consistent with optional reminders”
- adapts revision “by pages or by time”

Assessment against `Tathbeet`:

- very close on daily revision discipline
- weaker than `Tathbeet` on multi-profile management, shared learner profiles, and explicit offline-first family use
- simpler scope may make it feel faster and clearer than a more ambitious MVP

Sources:

- [Mudakir App Store listing](https://apps.apple.com/us/app/mudakir-quran-hifz-guide/id1527424936)

### 2. Hifdh Revision Tracker

Why it ranks highly:

- clearly focused on revision rather than full Quran reading
- supports targets using the same mental models many Huffadh already use: Surahs, Juzs, and Hizbs
- has a simple planner-and-checkoff loop

Best evidence found:

- set revision targets for `Surahs`, `Juzs`, and `Hizbs`
- display targets by day or month
- tick targets off as completed
- visualize how often memorized portions are being revised

Assessment against `Tathbeet`:

- strong benchmark for minimal schedule planning
- weaker than `Tathbeet` on profile model, collaboration, notifications depth, and wizard-based setup
- narrower unit support than `Tathbeet`, which also intends `rub al-hizb`

Sources:

- [Hifdh Revision Tracker App Store listing](https://apps.apple.com/us/app/hifdh-revision-tracker/id6477755380)

### 3. Makeen

Why it matters:

- directly frames the core problem as retention and daily revision difficulty
- positions itself as an automated answer to confusion about what to revise first
- has much larger visible traction than the smaller niche apps

Best evidence found:

- describes the real problem as consolidating memorized Quran over time
- says memorization requires “strict and intensive daily revision”
- highlights confusion about “choosing the first part to review”
- says the app can take over that role on the user's behalf

Assessment against `Tathbeet`:

- very relevant benchmark for the scheduling engine and daily decision-reduction
- stronger than `Tathbeet` on perceived automation value in the core revision loop
- weaker than `Tathbeet` on family/device-sharing workflows, explicit shared profile management, and detailed selection model from the PRD

Sources:

- [Makeen Google Play listing](https://play.google.com/store/apps/details?id=club.firmmind.makeen)

### 4. Muraajah Map

Why it is similar:

- explicitly about maintaining Hifz through revision
- includes daily goals and reminders
- adds mistake capture and post-session reports, which many serious users will value

Best evidence found:

- daily goal by pages or Juz
- morning or evening reminder
- recitation sessions with word-level or ayah-level mistake marking
- saved reports with mistakes, notes, and last-recited context

Assessment against `Tathbeet`:

- stronger than `Tathbeet` on in-session quality control and reflective review data
- weaker than `Tathbeet` on schedule generation, multi-profile management, and collaborative household use
- points to a meaningful product gap if `Tathbeet` ships with only binary done/not-done tracking

Sources:

- [Muraajah Map App Store listing](https://apps.apple.com/us/app/muraajah-map-hifz-revision-app/id6742591549)
- [Muraajah Map official site](https://muraajah-map.webflow.io/)

### 5. Tarteel

Why it still belongs in the list:

- it is broader than `Tathbeet`, but it is the most visible adjacent memorization product in the category
- users will compare any Quran memorization tool against it, even if the product scope differs

Best evidence found:

- AI-based mistake detection during recitation
- hidden-verse testing workflow
- goals for memorization, review, and general recitation
- automatic breakdown of goals into manageable sessions
- very large market presence

Assessment against `Tathbeet`:

- much stronger than `Tathbeet` on feedback, polish, scale, and perceived intelligence
- less focused than `Tathbeet` on offline-first daily revision planning for multiple learner profiles on one device
- sets a high expectation for modern memorization tools, even if its feature set is outside your MVP

Sources:

- [Tarteel Google Play listing](https://play.google.com/store/apps/details?id=com.mmmoussa.iqra)
- [Tarteel goals help article](https://support.tarteel.ai/hc/en-us/articles/32486640388877-How-do-I-use-the-Goals-feature)

## Comparison Matrix

| Product | Daily revision planning | Reminders | Progress tracking | Mistake capture / correction | Multi-profile / manager flow | Collaboration / sync | Scope fit vs. Tathbeet |
|---|---|---|---|---|---|---|---|
| Mudakir | Strong | Strong | Strong | Light | Not advertised | Not advertised | Very high |
| Hifdh Revision Tracker | Strong | Not advertised | Strong | None advertised | None advertised | None advertised | Very high |
| Makeen | Strong | Not clear from listing | Moderate | Not clear from listing | None advertised | None advertised | High |
| Muraajah Map | Moderate | Strong | Strong | Strong | None advertised | Sign-in present, but shared management not advertised | High |
| Tarteel | Strong | Not central in positioning | Strong | Very strong | None advertised | Account-based product, but shared learner management not advertised | Medium |
| Tathbeet (PRD) | Strong | Strong | Moderate in MVP | None in MVP | Very strong | Strong in planned MVP | Purpose-built |

## Consolidated Gap Analysis Against Tathbeet

### Where Tathbeet Is Already Differentiated on Paper

These are the strongest product wedges in the PRD and the main reasons `Tathbeet` can still matter in a market that already has memorization tools:

- `Tathbeet` is the only concept here built around `multi-profile management on one device` as a first-class workflow.
- `Tathbeet` explicitly supports `family members, teachers, and co-managers`, not just one learner working alone.
- `Tathbeet` defines `shared learner profiles` and cross-device collaboration in the product model, rather than leaving sync as an implied add-on.
- `Tathbeet` is `offline-first` for the core schedule and completion loop, which is a meaningful differentiator for reliability and trust.
- `Tathbeet` has a clearer and more rigorous scheduling model than the competitors described in store copy: user-visible selection preservation, overlap resolution, normalized coverage, and rollover of incomplete work.
- `Tathbeet` is intentionally `Arabic-first and RTL-first`, while several competitors appear English-first in their current store presence.

### Where Tathbeet Looks Weaker Than the Market

These are the biggest gaps visible when comparing the PRD to what competitors already surface:

- `Tathbeet` does not currently include `mistake capture`, `session notes`, or any kind of `quality-of-revision` signal. Several serious users will see binary completion as too shallow.
- `Tathbeet` does not include `live recitation feedback` or `post-recitation verification`. Tarteel especially raises user expectations here.
- The MVP only tracks `completion rate`, while competitors like Muraajah Map and Tarteel provide richer learning feedback loops.
- Some competitors communicate their value in a single sentence very clearly: “we decide what you revise,” “we track your mistakes,” or “we test your memorization.” `Tathbeet` is more ambitious, but its primary promise must stay equally crisp.

### Where Tathbeet Has the Clearest Product Opportunity

- Own the `household and teacher-manager use case`. None of the 5 benchmarked products clearly owns “one adult managing multiple learners on one device.”
- Own the `maintenance` niche, not the broader “Quran super app” category. The PRD is strongest when it stays tightly focused on revision sustainability.
- Turn schedule generation into the product headline. The best market opening is not “another memorization app”; it is “the app that decides today’s revision calmly and correctly.”
- Make `shared accountability without social noise` a core differentiator. Your PRD wants collaboration, but not public social features. That is a strong middle ground.

## Product Risks Exposed by the Competitor Set

### Risk 1: MVP may feel administratively strong but spiritually and practically thin

If the first version only lets users set a pool, see today’s task, and tap done, it may look less useful than competitors that actively help during recitation.

Implication:

- users may respect the planning model but still prefer another app during the actual revision session

Suggested response:

- keep MVP scope, but design a clear post-MVP path for `lightweight review notes`, `confidence marking`, or `mistake logging`

### Risk 2: Collaboration may be valuable but not immediately legible

Shared profiles and multi-manager support are important, but they are not as instantly understandable in an app listing as “AI correction” or “mistake marker.”

Implication:

- the product can be more differentiated than competitors and still lose if the messaging is too abstract

Suggested response:

- position `Tathbeet` in plain language as the easiest way to manage revision for yourself or your learners

### Risk 3: The scheduling model is sophisticated but invisible

Your PRD contains strong logic around overlap resolution and normalized coverage, but that strength is internal unless the UI makes the benefit obvious.

Implication:

- users may not notice the difference between a smart scheduler and a simple checklist app

Suggested response:

- expose the value through a very clear schedule summary: pool size, estimated rotation length, what rolled over, and why today’s plan is the right amount

## Recommendations for the PRD and Positioning

### 1. Tighten the one-line product promise

Suggested positioning:

`Tathbeet is the Quran revision planner for maintaining memorization consistently, for individuals and families.`

This is clearer and more ownable than a broader memorization-app framing.

### 2. Treat multi-profile and shared management as the main wedge, not a side feature

Most competitors are single-user tools. In your PRD, this is one of the rare genuinely differentiated bets. It should stay central in onboarding, feature priority, and messaging.

### 3. Add a lightweight quality signal after MVP

The best next feature after core scheduling is not full AI correction. It is something smaller:

- confidence after review
- quick mistake count
- short note per segment or per day

That would narrow the gap with Muraajah Map and Tarteel without blowing up scope.

### 4. Preserve the calm, low-cognitive-load direction

Mudakir shows there is value in a focused, low-distraction revision product. `Tathbeet` should resist turning into a reader, audio player, or content-heavy Quran app during MVP.

### 5. Make Arabic-first execution feel intentional, not merely translated

This is a product advantage if taken seriously. The strongest version of `Tathbeet` should feel designed for Arabic-speaking learners and families from the beginning, not localized afterward.

## Bottom Line

The strongest direct comparables are `Mudakir`, `Hifdh Revision Tracker`, and `Makeen`. The most important adjacent benchmark is `Tarteel`.

`Tathbeet` does not win by out-AI-ing Tarteel or out-instrumenting Muraajah Map. It wins if it becomes the clearest, calmest, most family-capable Quran revision planner: offline-first, Arabic-first, multi-profile, and genuinely helpful at deciding what to revise today.
