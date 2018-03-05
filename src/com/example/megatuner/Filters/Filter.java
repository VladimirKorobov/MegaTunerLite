package com.example.megatuner.Filters;

/**
 * Created with IntelliJ IDEA.
 * User: Vladimir-Desktop
 * Date: 16.10.13
 * Time: 23:52
 * To change this template use File | Settings | File Templates.
 */
public class Filter {
    public static class Vector {
        public double[] m_v = null;
        public Vector(int n)
        {
            m_v = new double[n];
        }
        public Vector(int n, int num, double koef)
        {
            m_v = new double[n];
            m_v[num] = koef;
        }
    }
    public static class Vector3 extends Vector
    {

        public Vector3()
        {
            super(3);
        }
        public Vector3(int num, double koef)
        {
            super(3, num, koef);
        }
        public static Vector3 Plus(Vector3 Vector1, Vector3 Vector2)
        {
            Vector3 v = new Vector3();
            for (int i = 0; i < v.m_v.length; i++)
                v.m_v[i] = Vector1.m_v[i] + Vector2.m_v[i];
            return v;
        }
        public static Vector3 Mult(Vector3 Vector, double coef)
        {
            Vector3 v = new Vector3();
            for (int i = 0; i < v.m_v.length; i++)
                v.m_v[i] = Vector.m_v[i] * coef;
            return v;
        }
    }
    public static class RK4
    {
        protected double m_k0, m_k1, m_k2;
        protected double m_m0, m_m1, m_m2;
        protected double m_Uder, m_Uout, m_Uout1;

        public void Init(double freq, double h, double q)
        {

        }

        public void InitWithoutOperators(double kIn, double kDer, double kOut, double h)
        {
            Vector3 Uin = new Vector3(0, 1.0);
            Vector3 Uout = new Vector3(1, 1.0);
            Vector3 Uder = new Vector3(2, 1.0);

            Vector3 K1;
            Vector3 K2;
            Vector3 K3;
            Vector3 K4;

            Vector3 M1;
            Vector3 M2;
            Vector3 M3;
            Vector3 M4;

            Vector3 k1, k2, k3;
            k1 = Vector3.Mult(Uin, kIn);
            k2 = Vector3.Mult(Uout, kOut);
            k3 = Vector3.Mult(Uder, kDer);

            K1 = Vector3.Plus(Vector3.Plus(k1, k2), k3);
            M1 = Uder;

            k1 = Vector3.Mult(Uin, kIn);
            k2 = Vector3.Mult(Vector3.Plus(Uout, Vector3.Mult(M1, h/2)), kOut);
            k3 = Vector3.Mult(Vector3.Plus(Uder, Vector3.Mult(K1, h/2)), kDer);

            K2 = Vector3.Plus(Vector3.Plus(k1, k2), k3);
            M2 = Vector3.Plus(Uder, Vector3.Mult(K1, h/2));

            k1 = Vector3.Mult(Uin, kIn);
            k2 = Vector3.Mult(Vector3.Plus(Uout, Vector3.Mult(M2, h / 2)), kOut);
            k3 = Vector3.Mult(Vector3.Plus(Uder, Vector3.Mult(K2, h / 2)), kDer);

            K3 = Vector3.Plus(Vector3.Plus(k1, k2), k3);
            M3 = Vector3.Plus(Uder, Vector3.Mult(K2, h / 2));

            k1 = Vector3.Mult(Uin, kIn);
            k2 = Vector3.Mult(Vector3.Plus(Uout, Vector3.Mult(M3, h)), kOut);
            k3 = Vector3.Mult(Vector3.Plus(Uder, Vector3.Mult(K3, h)), kDer);

            K4 = Vector3.Plus(Vector3.Plus(k1, k2), k3);
            M4 = Vector3.Plus(Uder, Vector3.Mult(K3, h));

            k1 = Vector3.Plus(M1, Vector3.Mult(M2, 2));
            k1 = Vector3.Plus(k1, Vector3.Mult(M3, 2));
            k1 = Vector3.Plus(k1, M4);
            k1 = Vector3.Mult(k1, (h / 6));
            Vector3 Uout1 = Vector3.Plus(Uout, k1);

            k1 = Vector3.Plus(K1, Vector3.Mult(K2, 2));
            k1 = Vector3.Plus(k1, Vector3.Mult(K3, 2));
            k1 = Vector3.Plus(k1, K4);
            k1 = Vector3.Mult(k1, (h / 6));
            Vector3 Uder1 = Vector3.Plus(Uder, k1);

            m_k0 = Uder1.m_v[0];
            m_k1 = Uder1.m_v[1];
            m_k2 = Uder1.m_v[2];

            m_m0 = Uout1.m_v[0];
            m_m1 = Uout1.m_v[1];
            m_m2 = Uout1.m_v[2];
        }

        public double Next(short[] input, int index)
        {
            return 0;
        }

    }

    public static class RK4FilterParallel extends RK4
    {
        double m_freq;
        double m_q;
        double m_h_1;
        double m_Uout_1;

        public void Init(double freq, double h, double q)
        {
            double w = 2 * Math.PI * freq;
            m_freq = freq;
            m_q = q;
            h *= 2;

            double kIn = q; //
            double kOut = -1;
            double kDer = -q;
            m_h_1 = 1.0 / (h * w);
            //Init(kIn, kDer, kOut, m_h);
            InitWithoutOperators(kIn, kDer, kOut, h * w);
        }


        public RK4FilterParallel(double freq, double h, double q)
        {
            Init(freq, h, q);
        }
        public double Next(short[] input, int index)
        {
            double Uin = input[index];

            double res = m_m0 * Uin + m_m1 * m_Uout + m_m2 * m_Uder;
            m_Uder = m_k0 * Uin + m_k1 * m_Uout + m_k2 * m_Uder;

            /*
            m_Uder = (m_k0 * Uin + m_k1 * (m_m0 * Uin + m_m1 * m_Uout ) + m_k2 * m_Uder) / (1 - m_k1 * m_m2);
            double res = m_m0 * Uin + m_m1 * m_Uout + m_m2 * m_Uder;
            */

            double outRes = (res - m_Uout1) / 2 * m_h_1;
            m_Uout_1 = m_Uout;
            m_Uout = res;
            return outRes;
        }
    }
}
