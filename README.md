# Tonal-Dissonance
**Author:** Mika Matsuyama
A computational model of tonal dissonance that integrates psychoacoustic roughness, harmonic context, and temporal neural stability. The system adapts to instrument timbre via overtone weighting, producing a continuous, context-aware measure of perceived tonal tension.

## Overview
This project presents a holistic computational model of consonance, dissonance, and tonal tension in Western tonal music. It integrates psychoacoustic roughness, harmonic context, and temporal neural stability into a single, continuous tension measure.

## Motivation
Most existing models analyze consonance and dissonance from a single perspective—psychoacoustic, harmonic, or temporal. Musical perception, however, emerges from the interaction of all three. This project unifies these approaches to better reflect how listeners perceive tension and resolution.

## Core Approaches

### Psychoacoustic Roughness
- Based on Plomp & Levelt’s critical bandwidth experiments
- Implemented using Sethares’ generalized roughness equation
- Normalized for comparison with other tension components

### Harmonic Context (Tonal Tension Profile)
- Uses 12D chroma vectors and 6D Tonal Interval Vectors (TIVs)
- Measures tonal distance, voice leading, and hierarchical tension
- Captures context-dependent harmonic motion across progressions

### Temporal Stability (RQA)
- Applies Recurrence Quantification Analysis to phase interactions
- Models second-order beating and neural phase instability
- Produces a time-dependent consonance–dissonance measure

## Integrated Model
The final tonal tension score combines seven components:
1. Distance from previous chord  
2. Distance from key  
3. Distance from tonal function  
4. Normalized psychoacoustic roughness  
5. Voice-leading smoothness  
6. Hierarchical tension  
7. Temporal instability (1 − RQA)

## Timbre Adaptation
A fixed psychoacoustic weight is dynamically split between roughness and temporal instability using an **overtone richness parameter (O)**:
- High O → roughness-dominant (e.g., violin, brass)
- Low O → temporal-dominant (e.g., flu
