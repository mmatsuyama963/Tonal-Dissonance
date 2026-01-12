public class DissonanceCalculator {

    public static double sethares(double[] freqs, double[] amplitudes) {
        double D = 0;
        int n = freqs.length;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                double f1 = freqs[i], f2 = freqs[j], a1 = amplitudes[i], a2 = amplitudes[j];
                double x = Math.abs(f2 - f1);
                double s = 0.24;
                D += a1 * a2 * (Math.exp(-3.5 * s * x) - Math.exp(-5.75 * s * x));
            }
        }
   

        return D*1000000000*50;

    }
    
    public static double normalSethares(double[] freqs, double[] amplitudes, double O) {
    int N = freqs.length;
    if (N < 2) return 0; // nothing to compare

    double D = 0;
    for (int i = 0; i < N; i++) {
        for (int j = i + 1; j < N; j++) {
            double f1 = freqs[i], f2 = freqs[j], a1 = amplitudes[i], a2 = amplitudes[j];
            double fMin = Math.min(f1, f2);
            double fMax = Math.max(f1, f2);
            double s = 0.24;
            double x = (fMax - fMin) / (s * fMin); // normalized
            D += a1 * a2 * (Math.exp(-3.5 * x) - Math.exp(-5.75 * x));
        }
    }

    // Maximum roughness normalization factor
    double maxPairs = (N * (N - 1)) / 2.0;
    double maxRoughness = 0.0349 * maxPairs * Math.pow(6, O);

    // Normalized and weighted Sethares score
    double Rsethares = (D / maxRoughness) * 0.425 * O;

    return Rsethares;
}


    public static int[] buildKeyChroma(String key, boolean isMajor) {
        int[] chroma = new int[12];
        int tonic = noteNameToPitchClass(key);
        int[] pattern = isMajor ? new int[]{0,2,4,5,7,9,11} : new int[]{0,2,3,5,7,8,10};
        for (int p : pattern) chroma[(tonic + p) % 12] = 1;
        return chroma;
    }

    private static int noteNameToPitchClass(String n) {
        return switch (n) {
            case "C" -> 0; case "C#" -> 1; case "D" -> 2; case "D#" -> 3; case "E" -> 4;
            case "F" -> 5; case "F#" -> 6; case "G" -> 7; case "G#" -> 8; case "A" -> 9;
            case "A#" -> 10; case "B" -> 11; default -> 0;
        };
    }

    public static double[] chromaToTIV(int[] chroma) {
        int N = 12;
        double[] tiv = new double[6];
        for (int k = 1; k <= 6; k++) {
            double real = 0, imag = 0;
            for (int n = 0; n < N; n++) {
                double angle = -2 * Math.PI * k * n / N;
                real += chroma[n] * Math.cos(angle);
                imag += chroma[n] * Math.sin(angle);
            }
            tiv[k-1] = Math.sqrt(real*real + imag*imag);
        }
        return tiv;
    }

    public static double euclideanDistance(double[] tiv1, double[] tiv2) {
        double sum = 0;
        for (int i = 0; i < tiv1.length; i++) sum += Math.pow(tiv1[i] - tiv2[i], 2);
        return Math.sqrt(sum);
    }

    public static double angleBetweenTIVs(double[] tiv1, double[] tiv2) {
        double dot=0, norm1=0, norm2=0;
        for(int i=0;i<tiv1.length;i++){
            dot += tiv1[i]*tiv2[i];
            norm1 += tiv1[i]*tiv1[i];
            norm2 += tiv2[i]*tiv2[i];
        }
        return Math.acos(dot/(Math.sqrt(norm1)*Math.sqrt(norm2)));
    }

    public static double voiceLeading(int[] chordPrev, int[] chordCurr) {
        int len = Math.min(chordPrev.length, chordCurr.length);
        double sum = 0;
        for(int i=0;i<len;i++) sum += Math.abs(chordCurr[i]-chordPrev[i]);
        return sum/len;
    }

    public static double ttpChord(int[] chordCurr, int[] chordPrev, int[] keyChroma) {
        double wTonalDist = 0.4, wDiss = 0.425, wVoice = 0.15, wHier = 0.075;
        int[] chromaCurr = chordToChroma(chordCurr);
        int[] chromaPrev = chordPrev != null ? chordToChroma(chordPrev) : new int[12];
        double[] tivCurr = chromaToTIV(chromaCurr);
        double[] tivPrev = chordPrev != null ? chromaToTIV(chromaPrev) : new double[6];
        double[] tivKey = chromaToTIV(keyChroma);
        double tonalDist = chordPrev != null ? euclideanDistance(tivCurr, tivPrev) : 0;
        double angle = angleBetweenTIVs(tivCurr, tivKey);
        double diss = 0; for(double x : tivCurr) diss += x;
        double vl = chordPrev != null ? voiceLeading(chordPrev, chordCurr) : 0;
        double hier = 1;
        return wTonalDist*tonalDist + wDiss*diss + wVoice*vl + wHier*hier;
    }

    private static int[] chordToChroma(int[] chord) {
        int[] chroma = new int[12];
        for(int n : chord) chroma[n % 12] = 1;
        return chroma;
    }









    public static double rqa(double[] freqs, double[] amps) {
        int N = freqs.length;
        double[][] phase = new double[N][1];
        for(int i=0;i<N;i++) phase[i][0]=freqs[i];
        double epsilon=1.0;
        double sum=0;
        for(int i=0;i<N;i++){
            for(int j=0;j<N;j++){
                double dist=Math.abs(phase[i][0]-phase[j][0]);
                if(dist<epsilon) sum+=1;
            }
        }
        return sum/(N*N);
    }
}