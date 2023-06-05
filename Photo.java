package PROJECT;

public class Photo extends ImageApplication {
	 static int size;
	 static int b;
	 static int l;
	 static int	InpImage[];
	 static int Histogram[];
	 static int CumulativeHist[];
	 static int OutImage[];	
	 /**
	 * @param size
	 * @param b
	 * @param l
	 * @param inpImage
	 * @param histogram
	 * @param cumulativeHist
	 * @param outImage
	 */
	public Photo(int size, int b, int l, int[] inpImage, int[] histogram, int[] cumulativeHist, int[] outImage) {
		super();
		Photo.size = size;
		Photo.b = b;
		Photo.l = l;
		InpImage = inpImage;
		Histogram = histogram;
		CumulativeHist = cumulativeHist;
		OutImage = outImage;
	}

	public static void main() {
		System.out.println("please input b");
		try {
			size = (2^b)-1;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(int i = 0; i<size-1; i++) {
			Histogram[InpImage[i]] = Histogram[InpImage[i]] + 1 ;
		}
		for (int i = 1; i<size-1; i++) {
			CumulativeHist[i]= CumulativeHist[i-1]+ Histogram[i]; 
		}
		for(int i = 0; i<size; i++) {
			CumulativeHist[i]= (CumulativeHist[i]/size)*l ;
		}
	}

	/**
	 * @return the b
	 */
	public int getB() {
		return b;
	}
	/**
	 * @param b the b to set
	 */
	public void setB(int b) {
		Photo.b = b;
	}
}
